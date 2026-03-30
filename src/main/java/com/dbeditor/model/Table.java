package com.dbeditor.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Table {
    private String name;
    private LinkedHashMap<String, Column> columns;
    private LinkedHashMap<String, ForeignKey> foreignKeys;
    
    public Table(String name) {
        this.name = name;
        this.columns = new LinkedHashMap<>();
        this.foreignKeys = new LinkedHashMap<>();
    }
    
    public void addColumn(Column column) {
        this.columns.put(column.getName(), column);
    }
    
    public void removeColumn(Column column) {
        this.columns.remove(column.getName());
    }
    
    public void addForeignKey(ForeignKey fk) { foreignKeys.add(fk); }
    
    public String getName() { return this.name; }
    public List<Column> getColumns() { return new ArrayList<>(this.columns.values()); }
    public Column getColumn(String name) { return this.columns.get(name); }
    public List<ForeignKey> getForeignKeys() { return this.foreignKeys; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder(this.getName() + "\n");

        if(!columns.isEmpty()) {
            res.append("column | pk : [\n");
            for(Column col : columns.values()) {
                res.append("\t" + col.getName() + " | " + col.isPrimaryKey() + "\n");
            } res.append("]\n");
        }

        if(!foreignKeys.isEmpty()) {
            res.append("foreign key : [\n");
            for(ForeignKey fk : foreignKeys) {
                res.append("\t" + fk.getFkName() + "\n");
            } res.append("]\n");
        }

        return res.toString() + "\n";
    }

    /* ================================================== */
    // pour que les tables aient la même position sur un changement de vue
    /* ================================================== */
    private float posX = 0;
    private float posY = 0;
    public void setPosX(float x) { this.posX = x; }
    public void setPosY(float y) { this.posY = y; }
    public float getPosX() { return this.posX; }
    public float getPosY() { return this.posY; }
}