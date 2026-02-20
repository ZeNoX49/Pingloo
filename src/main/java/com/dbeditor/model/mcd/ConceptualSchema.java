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