package com.dbeditor.model.mcd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dbeditor.MainApp;
import com.dbeditor.model.Column;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;

import javafx.util.Pair;

/**
 * Permet de représenter un MCD
 */
public class ConceptualSchema {
    private final Map<String, Entity> entities = new HashMap<>();
    private final Map<String, Association> associations = new HashMap<>();

    public ConceptualSchema(DatabaseSchema schema) {
        if(schema.tables.isEmpty()) return;

        // Identifier les entités et les associations
        // créer les tables et attendre avant de créer les assos
        List<Table> assoToCreate = new ArrayList<>();
        for(Table table : schema.tables.values()) {
            if(this.isAssociativeTable(table)) {
                assoToCreate.add(table);
            } else {
                this.entities.put(table.name, new Entity(table));
            }
        }

        // créer les associations (tables associatives)
        for(Table table : assoToCreate) {
            this.createAssociationFromTable(table);
        }

        // Ajouter les associations basées sur les FK des entités
        for(Entity entity : entities.values()) {
            for(ForeignKey fk : entity.table.getForeignKeys()) {
                // si != null alors lien entre table hors association
                Entity target = entities.get(fk.referencedTable);
                if(target == null) continue;

                // colonne FK dans la table référençante (entity)
                Column referencingCol = entity.table.columns.get(fk.columnName);
                if(referencingCol == null) continue;

                // Déterminer la cardinalité côté référençant (entity)
                // chaque ligne référençante pointe vers une et une seule entité référence -> max = 1
                // min = 1 si NOT NULL sinon 0
                CardinalityValue referencingCard = referencingCol.isNotNull ? CardinalityValue._11_ : CardinalityValue._01_;

                // Déterminer la cardinalité côté référencé (target)
                // par défaut B peut être référencé par plusieurs A -> 0..N
                // si la colonne FK dans A est unique (détectée ici si elle est PK), alors max = 1
                CardinalityValue targetCard;
                if (referencingCol.isPrimaryKey) {
                    // FK est PK => relation 1-1 ou 0-1 selon nullabilité de la FK (dans A)
                    targetCard = referencingCol.isNotNull ? CardinalityValue._11_ : CardinalityValue._01_;
                } else {
                    Column fkColumn = entity.table.columns.get(fk.columnName);
                    // FK non-unique => côté référencé = 0..N (par défaut pas d'obligation)
                    targetCard = fkColumn.isNotNull ? CardinalityValue._1N_ : CardinalityValue._0N_;
                }

                List<Pair<Entity, CardinalityValue>> linkedEntitiesCard = new ArrayList<>();
                // ordre : premier = référençant (entity), second = référencé (target)
                linkedEntitiesCard.add(new Pair<>(entity, referencingCard));
                linkedEntitiesCard.add(new Pair<>(target, targetCard));

                Association association = new Association(linkedEntitiesCard, null);
                this.associations.put(association.referencedTable.name, association);
            }
        }
    }

    /**
     * Vérifie si la table est un association
     * telle que -> (pk === fk) >= 2
     */
    private boolean isAssociativeTable(Table table) {
        List<Column> tablePkList = new ArrayList<>();
        for(Column col : table.getColumns()) {
            if(col.isPrimaryKey) {
                tablePkList.add(col);
            }
        }

        if(tablePkList.isEmpty()) return false;
        Set<String> pkCols = new HashSet<>();
        for(Column c : tablePkList) pkCols.add(c.name);
        Set<String> fkCols = new HashSet<>();
        for(ForeignKey fk : table.getForeignKeys()) fkCols.add(fk.columnName);

        return pkCols.equals(fkCols) && fkCols.size() >= 2;
    }

    /**
     * Permet de créer les associations (tables d'association -> relation n-n)
     */
    private void createAssociationFromTable(Table table) {
        List<Pair<Entity, CardinalityValue>> linkedEntitiesCard = new ArrayList<>();
        for(ForeignKey fk : table.getForeignKeys()) {
            Entity target = entities.get(fk.referencedTable);
            if(target == null) continue;
            
            // Pour une table associative, côté entité la multiplicité est généralement 0..N
            // (une entité peut ne participer à aucune association ou à plusieurs)
            Column fkColumn = target.table.columns.get(fk.columnName);
            CardinalityValue cardForEntity;
            if (fkColumn.isNotNull) {
                cardForEntity = CardinalityValue._1N_;
            } else {
                cardForEntity = CardinalityValue._0N_;
            }
            linkedEntitiesCard.add(new Pair<>(target, cardForEntity));
        }
        this.associations.put(table.name, new Association(linkedEntitiesCard, table));
    }

    public boolean nameExists(String name) {
        return this.entities.containsKey(name) || this.associations.containsKey(name);
    }

    /**
     * Permet d'ajouter une entité.
     * L'ajoute aussi dans le schema de MainApp
     */
    public void addEntity(Table table) {
        this.entities.put(table.name, new Entity(table));
        MainApp.schema.addTable(table);
    }

    /**
     * Met à jour une entité et toutes les associations associés.
     * Le fait aussi dans le schema de MainApp
     */
    public void updateEntity(String oldName, Table updatedTable) {
        Entity old = entities.remove(oldName);
        if (old == null) return;

        Entity updated = new Entity(updatedTable);
        entities.put(updatedTable.name, updated);

        MainApp.schema.tables.remove(oldName);
        MainApp.schema.addTable(updatedTable);

        // mettre à jour les associations qui référencent l'ancienne entité
        for (Association assoc : this.associations.values()) {
            CardinalityValue card = assoc.linkedEntities.remove(old);
            if (card != null) {
                assoc.linkedEntities.put(updated, card);
            }
        }
    }

    /**
     * Supprime une entité et toutes les associations associés.
     * La supprime aussi dans le schema de MainApp
     */
    public void removeEntity(String name) {
        Entity e = entities.remove(name);
        if (e == null) return;

        MainApp.schema.tables.remove(name);

        // supprimer les associations qui contiennent cette entité
        this.associations.entrySet().removeIf(entry -> entry.getValue().linkedEntities.containsKey(e));
    }

    /**
     * Ajoute une association entre des entités
     * @return table utilisée pour l'affichage
     */
    public Table addAssociation(String name, List<Pair<String, CardinalityValue>> links) {
        Table table = new Table(name);

        List<Pair<Entity, CardinalityValue>> en = new ArrayList<>();
        for(Pair<String, CardinalityValue> p : links) {
            en.add(new Pair<>(this.entities.get(p.getKey()), p.getValue()));
        }

        this.associations.put(table.name, new Association(en, table));

        // TODO: modification de MainApp.schema

        return table;
    }

    // TODO
    // /**
    //  * Modifie les participants d'une association existante.
    //  * Utile après édition dans AssociationEditorDialog.
    //  */
    // public void updateAssociation(String name, String newName, Map<Entity, CardinalityValue> participants) {
    //     Association old = associations.remove(name);
    //     if (old == null) return;
    //     Association updated = new Association(newName, participants, old.referencedTable);
    //     associations.put(newName, updated);
    //
    //     // TODO: modification de MainApp.schema
    // }

    /**
     * Supprime une association par son nom.
     */
    public void removeAssociation(String name) {
        // au cas où elle est supprimé lors de la suppression d'une table 
        if(this.nameExists(name)) associations.remove(name);

        // TODO: (modification de MainApp.schema) a verif
        if(!MainApp.schema.tables.containsKey(name)) return;
        MainApp.schema.tables.remove(name);
        for(Table t : MainApp.schema.tables.values()) {
            for(ForeignKey fk : t.getForeignKeys()) {
                if(!fk.referencedTable.equals(name)) {
                    t.foreignKeys.remove(fk.columnName);
                }
            }
        }
    }
    
    /**
     * Retourne toutes les tables associés aux entités
     */
    public List<Table> getEntitiesTables() {
        List<Table> tables = new ArrayList<>();
        for(Entity e : this.entities.values()) {
            tables.add(e.table);
        } return tables;
    }

    /**
     * Retourne la table associé au nom de l'entité,
     * null si elle n'existe pas
     */
    public Table getEntityTable(String name) {
        Entity e = this.entities.get(name);
        return e == null ? null : e.table;
    }

    /**
     * Retourne la table associé au nom de l'entité,
     * null si elle n'existe pas
     */
    public Table getAssociationTable(String name) {
        Association a = this.associations.get(name);
        return a == null ? null : a.referencedTable;
    }
    
    /**
     * retourne le nécessaire pour tracé les liens entre les entités et les associations + les cardinalités
     */
    public Map<String, List<Pair<Table, CardinalityValue>>> getLinks() {
        Map<String, List<Pair<Table, CardinalityValue>>> links = new HashMap<>();
        for(Association assoc : this.associations.values()) {
            List<Pair<Table, CardinalityValue>> tablesCard = new ArrayList<>();
            for(Entity entity : assoc.linkedEntities.keySet()) {
                tablesCard.add(new Pair<>(entity.table, assoc.linkedEntities.get(entity)));
            }

            links.put(
                assoc.referencedTable.name,
                tablesCard
            );
        }
        return links;
    }

    /* =========================================================================================== */

    private final class Entity {
        public final Table table;

        public Entity(Table table) {
            this.table = table;
        }
    }

    private final class Association {
        public final Map<Entity, CardinalityValue> linkedEntities = new HashMap<>();
        public final Table referencedTable;

        public Association(List<Pair<Entity, CardinalityValue>> entitiesCard, Table rfTable) {
            if(rfTable != null) {
                this.referencedTable = rfTable;
            } else {
                String nom = "";
                for(Pair<Entity, CardinalityValue> p : entitiesCard) { nom += p.getKey().table.name + "_"; }
                this.referencedTable = new Table(nom);
            }

            for(Pair<Entity, CardinalityValue> p : entitiesCard) {
                this.linkedEntities.put(p.getKey(), p.getValue());
            }
        }
    }
}