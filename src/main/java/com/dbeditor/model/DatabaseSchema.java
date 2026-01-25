package com.dbeditor.model;

import java.util.HashMap;
import java.util.Map;

public class DatabaseSchema {
    private String name;
    private Map<String, Table> tables;
    
    public DatabaseSchema(String name) {
        this.name = name;
        if(name == null || name.isBlank()) { this.name = "db"; }
        this.tables = new HashMap<>();
    }
    
    public void setName(String name) { this.name = name; }
    public void addTable(Table table) { this.tables.put(table.getName(), table); }
    public void removeTable(String name) { this.tables.remove(name); }

    public String getName() { return this.name; }
    public Table getTable(String name) { return this.tables.get(name); }
    public Map<String, Table> getTables() { return this.tables; }

    public void clear() {
        this.name = "db";
        this.tables.clear();
    }
}