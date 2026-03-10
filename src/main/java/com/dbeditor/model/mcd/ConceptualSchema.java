package com.dbeditor.model.mcd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;

import javafx.util.Pair;

/**
 * Représente un MCD
 */
public class ConceptualSchema {
    private String name;
    private Map<String, Entity> entities;
    private List<Association> associations;
    
    // public ConceptualSchema(DatabaseSchema schema) {
    //     this.name = schema.getName();
    //     this.entities = new HashMap<>();
    //     this.associations = new ArrayList<>();

    //     if(schema.getTables().isEmpty()) return;

    //     // 1. Identifier les entités et les tables associatives
    //     List<Table> assoToCreate = new ArrayList<>();
    //     for(Table table : schema.getTables().values()) {
    //         if(isAssociativeTable(table)) {
    //             assoToCreate.add(table);
    //         } else {
    //             entities.put(table.getName(), new Entity(table));
    //         }
    //     }

    //     for(Table table : assoToCreate) {
    //         createAssociationFromTable(table);
    //     }

    //     // 2. Ajouter les associations basées sur les FK des entités
    //     for(Entity entity : entities.values()) {
    //         for(ForeignKey fk : entity.table.getForeignKeys()) {
    //             Entity target = entities.get(fk.getReferencedTable());
    //             if(target != null) {
    //                 Association assoc = new Association(fk.getFkName(), entity, target);
    //                 // Déduire cardinalité min/max
    //                 assoc.minFrom = entityHasNotNullFK(entity.table, fk) ? 1 : 0;
    //                 assoc.maxFrom = isUniqueFK(entity.table, fk) ? 1 : -1; // -1 = N
    //                 associations.add(assoc);
    //             }
    //         }
    //     }
    // }

    // /** Vérifie si la table est associative (PK = combinaison exclusive de FK) */
    // private boolean isAssociativeTable(Table table) {
    //     if(table.getPrimaryKeyColumns().isEmpty()) return false;
    //     Set<String> pkCols = new HashSet<>();
    //     for(Column c : table.getPrimaryKeyColumns()) pkCols.add(c.getName());
    //     Set<String> fkCols = new HashSet<>();
    //     for(ForeignKey fk : table.getForeignKeys()) fkCols.add(fk.getColumnName());

    //     return pkCols.equals(fkCols) && fkCols.size() >= 2;
    // }

    // /** Crée une association M-N à partir d'une table associative */
    // private void createAssociationFromTable(Table table) {
    //     List<Entity> linkedEntities = new ArrayList<>();
    //     for(ForeignKey fk : table.getForeignKeys()) {
    //         Entity target = entities.get(fk.getReferencedTable());
    //         if(target != null) linkedEntities.add(target);
    //     }
    //     if(linkedEntities.size() >= 2) {
    //         associations.add(new Association(linkedEntities, table));
    //     }
    // }

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

    // public List<Table> getTables() {
    //     List<Table> tables = new ArrayList<>();
    //     for(Entity e : entities.values()) {
    //         tables.add(e.table);
    //     }
    //     return tables;
    // }

    // public Table getTable(String name) {
    //     for(Entity e : entities.values()) {
    //         if(e.table.getName().equals(name)) {
    //             return e.table;
    //         }
    //     } return null;
    // }

    // public Map<String, Pair<Table, Table>> getLinks() {
    //     Map<String, Pair<Table, Table>> links = new HashMap<>();
    //     for(Association assoc : associations) {
    //         links.put(
    //             assoc.name,
    //             new Pair<>(assoc.entities.get(0).table, assoc.entities.get(1).table)
    //         );
    //     }
    // }

    public ConceptualSchema(DatabaseSchema schema) {
        this.name = schema.getName();

        this.entities = new HashMap<>();
        this.associations = new ArrayList<>();

        if(schema.getTables().isEmpty()) return;

        Map<String, List<ForeignKey>> fkList = new HashMap<>();
        for(Table table : schema.getTables().values()) {
            this.addTable(table);
            
            // on récupère les foreign keys pour pouvoir créer les associations
            String tableName = table.getName();
            fkList.put(tableName, new ArrayList<>());
            fkList.get(tableName).addAll(table.getForeignKeys());
        }

        for(String tableName : fkList.keySet()) {
            for(ForeignKey fk : fkList.get(tableName)) {
                Entity entity1 = entities.get(tableName);
                Entity entity2 = entities.get(fk.getReferencedTable());

                associations.add(new Association(entity1, entity2));
            }
        }
    }

    public void addTable(Table table) {
        entities.put(table.getName(), new Entity(table));
    }

    private class Entity {
        public final Table table;

        public Entity(Table table) {
            this.table = table;
        }
    }

    private class Association {
        public final Pair<Entity, Entity> link;

        public Association(Entity entity1, Entity entity2) {
            this.link = new Pair<>(entity1, entity2);
        }
    }

    public DatabaseSchema transformToDatabase() {
        // TODO
        return new DatabaseSchema(null);
    }

    public List<Table> getTables() {
        List<Table> tables = new ArrayList<>();
        for(Entity entity : this.entities.values()) {
            tables.add(entity.table);
        }
        return tables;
    }

    public Table getTable(String name) {
        Entity entity = this.entities.get(name);
        if(entity == null) return null;
        return entity.table;
    }

    public List<Pair<Table, Table>> getLinks() {
        List<Pair<Table, Table>> links = new ArrayList<>();

        for(Association association : this.associations) {
            Pair<Entity, Entity> p = association.link;
            links.add(new Pair<>(p.getKey().table, p.getValue().table));
        }

        return links;
    }
}