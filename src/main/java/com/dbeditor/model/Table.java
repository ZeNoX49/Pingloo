package com.dbeditor.model;

import java.util.ArrayList;
import java.util.List;

public class Table {
    private String name;
    private List<Column> columns;
    private List<ForeignKey> foreignKeys;
    
    public Table(String name) {
        this.name = name;
        this.columns = new ArrayList<>();
        this.foreignKeys = new ArrayList<>();
    }
    
    /**
     * Utilise pour la duplication de table
     * @param table table copié
     */
    public Table(Table table) {
        this.name = table.name;
        this.columns = new ArrayList<>();
        for (Column col : table.columns) {
            this.columns.add(new Column(col));
        }
        this.foreignKeys = new ArrayList<>(table.foreignKeys);
    }
    
    public void addColumn(Column column) {
        this.columns.add(column);
    }
    
    public void removeColumn(Column column) {
        this.columns.remove(column);
    }
    
    public void addForeignKey(ForeignKey fk) { foreignKeys.add(fk); }
    
    public String getName() { return this.name; }
    public List<Column> getColumns() { return this.columns; }
    public List<ForeignKey> getForeignKeys() { return this.foreignKeys; }
    
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return this.getName();
    }
}