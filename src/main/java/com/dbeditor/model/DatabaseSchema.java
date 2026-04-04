package com.dbeditor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dbeditor.sql.DbType;

public class DatabaseSchema {
    public String name;
    public DbType type;
    public final Map<String, Table> tables;
    
    public DatabaseSchema(String name) {
        this.name = name;
        if(name == null || name.isBlank()) this.name = "db";
        this.type = DbType.MySql;
        this.tables = new HashMap<>();
    }

    public void addTable(Table table) {
        this.tables.put(table.name, table);
    }
    public List<Table> getTables() {
        return new ArrayList<>(this.tables.values());
    }
}