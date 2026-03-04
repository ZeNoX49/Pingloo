package com.dbeditor.model.mcd;

import java.util.*;
import com.dbeditor.model.*;

import javafx.util.Pair;

/**
 * Transforme un MLD (DatabaseSchema) en MCD (ConceptualSchema)
 */
public class ConceptualSchema {
    private String name;
    private Map<String, Entity> entities;
    private List<Association> associations;

    public ConceptualSchema(DatabaseSchema schema) {
        this.name = schema.getName();
        this.entities = new HashMap<>();
        this.associations = new ArrayList<>();

        if(schema.getTables().isEmpty()) return;

        // 1. Identifier les entités et les tables associatives
        for(Table table : schema.getTables().values()) {
            if(isAssociativeTable(table)) {
                createAssociationFromTable(table);
                System.out.println(table.getName() + " est une association");
            } else {
                entities.put(table.getName(), new Entity(table));
                System.out.println(table.getName() + " est une entité");
            }
            System.out.println(table);
        }

        // 2. Ajouter les associations basées sur les FK des entités
        for(Entity entity : entities.values()) {
            for(ForeignKey fk : entity.table.getForeignKeys()) {
                Entity target = entities.get(fk.getReferencedTable());
                if(target != null) {
                    Association assoc = new Association(entity, target);
                    // Déduire cardinalité min/max
                    assoc.minFrom = entityHasNotNullFK(entity.table, fk) ? 1 : 0;
                    assoc.maxFrom = isUniqueFK(entity.table, fk) ? 1 : -1; // -1 = N
                    associations.add(assoc);
                }
            }
        }
    }

    /** Vérifie si la table est associative (PK = combinaison exclusive de FK) */
    private boolean isAssociativeTable(Table table) {
        if(table.getPrimaryKeyColumns().isEmpty()) return false;
        Set<String> pkCols = new HashSet<>();
        for(Column c : table.getPrimaryKeyColumns()) pkCols.add(c.getName());
        Set<String> fkCols = new HashSet<>();
        for(ForeignKey fk : table.getForeignKeys()) fkCols.add(fk.getColumnName());

        return pkCols.equals(fkCols) && fkCols.size() >= 2;
    }

    /** Crée une association M-N à partir d'une table associative */
    private void createAssociationFromTable(Table table) {
        List<Entity> linkedEntities = new ArrayList<>();
        for(ForeignKey fk : table.getForeignKeys()) {
            Entity target = entities.get(fk.getReferencedTable());
            if(target != null) linkedEntities.add(target);
        }
        if(linkedEntities.size() >= 2) {
            associations.add(new Association(linkedEntities, table));
        }
    }

    private boolean entityHasNotNullFK(Table table, ForeignKey fk) {
        for(Column col : table.getColumns()) {
            if(col.getName().equals(fk.getColumnName())) return col.isNotNull();
        }
        return false;
    }

    private boolean isUniqueFK(Table table, ForeignKey fk) {
        for(Column col : table.getColumns()) {
            if(col.getName().equals(fk.getColumnName())) return col.isUnique();
        }
        return false;
    }

    public List<Table> getTables() {
        List<Table> tables = new ArrayList<>();
        for(Entity e : entities.values()) tables.add(e.table);
        return tables;
    }

    public Table getTable(String name) {
        for(Entity e : entities.values()) {
            if(e.table.getName().equals(name)) {
                return e.table;
            }
        } return null;
    }

    public List<Pair<Table, Table>> getLinks() {
        List<Pair<Table, Table>> links = new ArrayList<>();
        for(Association assoc : associations) {
            if(assoc.entities.size() == 2) {
                links.add(new Pair<>(assoc.entities.get(0).table, assoc.entities.get(1).table));
            }
        }
        return links;
    }

    // ----------------- Classes internes -----------------

    private class Entity {
        public final Table table;

        public Entity(Table table) {
            this.table = table;
        }
    }

    private class Association {
        public List<Entity> entities;
        public Table associativeTable; // si M-N matérialisée
        public int minFrom = 0;
        public int maxFrom = -1; // -1 = N

        // Constructeur simple binaire
        public Association(Entity e1, Entity e2) {
            this.entities = Arrays.asList(e1, e2);
        }

        // Constructeur pour table associative (M-N)
        public Association(List<Entity> linkedEntities, Table table) {
            this.entities = linkedEntities;
            this.associativeTable = table;
        }
    }
}