package com.dbeditor.model.mcd;

import java.util.*;
import com.dbeditor.model.*;

import javafx.util.Pair;

/**
 * Transforme un MLD (DatabaseSchema) en MCD (ConceptualSchema)
 */
public class ConceptualSchema {
    private final Map<String, Entity> entities = new HashMap<>();
    private final Map<String, Association> associations = new HashMap<>();

    public ConceptualSchema(DatabaseSchema schema) {
        if(schema.getTables().isEmpty()) return;

        // Identifier les entités et les tables associatives
        // créer les tables et attendre avant de créer les assos
        List<Table> assoToCreate = new ArrayList<>();
        for(Table table : schema.getTables().values()) {
            if(isAssociativeTable(table)) {
                assoToCreate.add(table);
            } else {
                entities.put(table.getName(), new Entity(table));
            }
        }

        // créer les associations
        for(Table table : assoToCreate) {
            createAssociationFromTable(table);
        }

        // // Ajouter les associations basées sur les FK des entités
        // for(Entity entity : entities.values()) {
        //     for(ForeignKey fk : entity.table.getForeignKeys()) {
        //         Entity target = entities.get(fk.getReferencedTable());
        //         if(target != null) {
        //             Association assoc = new Association(fk.getFkName(), entity, target);
        //             // Déduire cardinalité min/max
        //             assoc.minFrom = entityHasNotNullFK(entity.table, fk) ? 1 : 0;
        //             assoc.maxFrom = isUniqueFK(entity.table, fk) ? 1 : -1; // -1 = N
        //             associations.add(assoc);
        //         }
        //     }
        // }
    }

    /**
     * Vérifie si la table est un association
     * telle que -> (pk == fk) >= 2
     */
    private boolean isAssociativeTable(Table table) {
        List<Column> tablePkList = new ArrayList<>();
        for(Column col : table.getColumns()) {
            if(col.isPrimaryKey()) {
                tablePkList.add(col);
            }
        }

        if(tablePkList.isEmpty()) return false;
        Set<String> pkCols = new HashSet<>();
        for(Column c : tablePkList) pkCols.add(c.getName());
        Set<String> fkCols = new HashSet<>();
        for(ForeignKey fk : table.getForeignKeys()) fkCols.add(fk.getColumnName());

        return pkCols.equals(fkCols) && fkCols.size() >= 2;
    }

    /**
     * Permet de créer les associations
     */
    private void createAssociationFromTable(Table table) {
        List<Pair<Entity, CardinalityValue>> linkedEntitiesCard = new ArrayList<>();
        for(ForeignKey fk : table.getForeignKeys()) {
            Entity target = entities.get(fk.getReferencedTable());
            if(target == null) continue;
            
            Column pk = null;
            for(Column c : target.table.getColumns()) {
                if(c.isPrimaryKey()) {
                    pk = c;
                    break;
                }
            }
            if(pk == null) continue;

            CardinalityValue card = pk.isNotNull() ? CardinalityValue._1N_ : CardinalityValue._0N_;
            linkedEntitiesCard.add(new Pair<>(target, card));
        }
        Association association = new Association(linkedEntitiesCard, table);
        associations.put(association.name, association);
    }

    // private boolean entityHasNotNullFK(Table table, ForeignKey fk) {
    //     for(Column col : table.getColumns()) {
    //         if(col.getName().equals(fk.getColumnName())) return col.isNotNull();
    //     }
    //     return false;
    // }

    // private boolean isUniqueFK(Table table, ForeignKey fk) {
    //     for(Column col : table.getColumns()) {
    //         if(col.getName().equals(fk.getColumnName())) return col.isUnique();
    //     }
    //     return false;
    // }

    /**
     * Retourne toutes les tables associés aux entités
     */
    public List<Table> getTables() {
        List<Table> tables = new ArrayList<>();
        for(Entity e : entities.values()) {
            tables.add(e.table);
        }
        return tables;
    }

    /**
     * Retourne la table associé au nom de l'entité,
     * null si elle n'existe pas
     */
    public Table getTable(String name) {
        for(Entity e : entities.values()) {
            if(e.table.getName().equals(name)) {
                return e.table;
            }
        } return null;
    }

    /**
     * retourne le nécessaire pour tracé les liens entre les entités et les associations + les cardinalités
     */
    public Map<String, List<Pair<Table, CardinalityValue>>> getLinks() {
        Map<String, List<Pair<Table, CardinalityValue>>> links = new HashMap<>();
        for(Association assoc : associations.values()) {
            List<Pair<Table, CardinalityValue>> tablesCard = new ArrayList<>();
            for(Entity entity : assoc.linkedEntities.keySet()) {
                tablesCard.add(new Pair<>(entity.table, assoc.linkedEntities.get(entity)));
            }

            links.put(
                assoc.name,
                tablesCard
            );
        }
        return links;
    }

    /**
     * Retourne la table associé au nom de l'entité,
     * null si elle n'existe pas
     */
    public Table getAssociationTable(String name) {
        for(Association a : associations.values()) {
            if(a.referencedTable.getName().equals(name)) {
                return a.referencedTable;
            }
        } return null;
    }

    /* =========================================================================================== */

    private class Entity {
        public final Table table;

        public Entity(Table table) {
            this.table = table;
        }
    }

    private class Association {
        public String name;
        public final Map<Entity, CardinalityValue> linkedEntities = new HashMap<>();
        public final Table referencedTable;

        public Association(List<Pair<Entity, CardinalityValue>> entitiesCard, Table rfTable) {
            if(rfTable != null) {
                this.referencedTable = rfTable;
                this.name = rfTable.getName();
            } else {
                String nom = "";
                for(Pair<Entity, CardinalityValue> p : entitiesCard) { nom += p.getKey().table.getName() + "_"; }
                this.referencedTable = new Table(nom);
                this.name = nom;
            }

            for(Pair<Entity, CardinalityValue> p : entitiesCard) {
                this.linkedEntities.put(p.getKey(), p.getValue());
            }
        }
    }
}