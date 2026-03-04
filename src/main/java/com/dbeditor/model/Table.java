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
    
    public List<Column> getPrimaryKeyColumns() {
        List<Column> res = new ArrayList<>();
        for(Column col : columns) {
            if(col.isPrimaryKey()) {
                res.add(col);
            }
        } return res;
    }

    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder(this.getName() + "\n");

        res.append("primary key : [\n");
        for(Column col : columns) {
            res.append("\t" + col.getName() + " | " + col.isPrimaryKey() + "\n");
        }
        res.append("]\n");

        res.append("foreign key : [\n");
        for(ForeignKey fk : foreignKeys) {
            res.append("\t" + fk.getFkName() + "\n");
        }
        res.append("]\n");

        return res.toString();
    }
}