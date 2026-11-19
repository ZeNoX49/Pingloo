package com.dbeditor.model;

import java.util.ArrayList;
import java.util.List;

import com.dbeditor.sql.DbType;

public class DatabaseSchema {

    public String name;
    public DbType type;
    private final List<Table> tables;

    public DatabaseSchema(String name) {
        this.name = name;
        if (name == null || name.isBlank()) {
            this.name = "db";
        }
        this.type = DbType.MySql;
        this.tables = new ArrayList<>();
    }

    public void addTable(Table table) {
        this.tables.add(table);
    }

    public void removeTable(Table table) {
        this.tables.remove(table);
    }

    /**
     * @return une copie de la liste des tables
     */
    public List<Table> getTables() {
        return new ArrayList<>(this.tables);
    }

}
