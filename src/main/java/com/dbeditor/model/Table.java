package com.dbeditor.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class Table {
    public String name;
    public final LinkedHashMap<String, Column> columns;
    public final LinkedHashMap<String, ForeignKey> foreignKeys;
    
    public Table(String name) {
        this.name = name;
        this.columns = new LinkedHashMap<>();
        this.foreignKeys = new LinkedHashMap<>();
    }

    public Table(Table other) {
        this(other.name);
        for(Column col : other.columns.values()) this.addColumn(new Column(col));
        for(ForeignKey fk : other.foreignKeys.values()) this.addForeignKey(new ForeignKey(fk));
    }

    public void addColumn(Column col) {
        this.columns.put(col.name, col);
    }
    public List<Column> getColumns() {
        return new ArrayList<>(this.columns.values());
    }

    public void addForeignKey(ForeignKey fk) {
        this.foreignKeys.put(fk.columnName, fk);
    }
    public List<ForeignKey> getForeignKeys() {
        return new ArrayList<>(this.foreignKeys.values());
    }

    /* =================================================================== */
    // pour que les tables aient la même position sur un changement de vue //
    /* =================================================================== */
    public double posX = 0;
    public double posY = 0;
}