package com.dbeditor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseSchema {
    public String name;
    public final Map<String, Table> tables;
    
    public DatabaseSchema(String name) {
        this.name = name;
        if(name == null || name.isBlank()) this.name = "db";
        this.tables = new HashMap<>();
    }

    public List<Table> getTables() {
        return new ArrayList<>(this.tables.values());
    }
}