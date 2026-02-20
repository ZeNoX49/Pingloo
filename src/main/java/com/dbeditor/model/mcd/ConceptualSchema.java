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

        if(schema.getTables().isEmpty()) return;

        Map<String, List<ForeignKey>> fkList = new HashMap<>();
        for(Table table : schema.getTables().values()) {
            String name = table.getName();
            entities.put(name, new Entity(table));

            fkList.put(name, new ArrayList<>());
            fkList.get(name).addAll(table.getForeignKeys());
        }

        for(String name : fkList.keySet()) {
            for(ForeignKey fk : fkList.get(name)) {
                Entity entity1 = entities.get(name);
                Entity entity2 = entities.get(fk.getReferencedTable());

                associations.add(new Association(entity1, entity2));
            }
        }
    }

    private class Entity {
        public Table table;

        public Entity(Table table) {
            this.table = table;
        }
    }

    private class Association {
        public Pair<Entity, Entity> link;

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

    public List<Pair<Table, Table>> getLinks() {
        List<Pair<Table, Table>> links = new ArrayList<>();

        for(Association association : this.associations) {
            Pair<Entity, Entity> p = association.link;
            links.add(new Pair<>(p.getKey().table, p.getValue().table));
        }

        return links;
    }
}