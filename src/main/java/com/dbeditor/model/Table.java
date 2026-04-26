package com.dbeditor.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class Table {
    public String name;
    private final LinkedHashMap<String, Boolean> attributs; // si true -> colonne
    public final LinkedHashMap<String, Column> columns;
    public final LinkedHashMap<String, ForeignKey> foreignKeys;
    
    public Table(String name) {
        this.name = name;
        this.attributs = new LinkedHashMap<>();
        this.columns = new LinkedHashMap<>();
        this.foreignKeys = new LinkedHashMap<>();
    }

    public Table(Table other) {
        this(other.name);
        for(Column col : other.columns.values()) this.addColumn(new Column(col));
        for(ForeignKey fk : other.foreignKeys.values()) this.addForeignKey(new ForeignKey(fk));
        if(other.positionned) this.setPosition(other.posX, other.posY);
    }

    public void addColumn(Column col) {
        this.columns.put(col.name, col);
        this.attributs.put(col.name, true);
    }
    public List<Column> getColumns() {
        return new ArrayList<>(this.columns.values());
    }

    public void addForeignKey(ForeignKey fk) {
        this.foreignKeys.put(fk.columnName, fk);
        // la colonne sera (surement) déja référencé, donc on l'enlève
        if(this.attributs.get(fk.columnName)) {
            fk.isPrimaryKey = this.columns.get(fk.columnName).isPrimaryKey;
            this.columns.remove(fk.columnName);
        }
        this.attributs.put(fk.columnName, false);
    }
    public List<ForeignKey> getForeignKeys() {
        return new ArrayList<>(this.foreignKeys.values());
    }

    public LinkedHashMap<String, Boolean> getAttributs() {
        return new LinkedHashMap<>(this.attributs);
    }

    /* =================================================================== */
    // pour que les tables aient la même position sur un changement de vue //
    /* =================================================================== */
    private double posX = 0;
    private double posY = 0;
    private boolean positionned = false;

    public void setPosition(double x, double y) {
        this.posX = x;
        this.posY = y;
        this.positionned = true;
    }

    public double getPosX() { return this.posX; }
    public double getPosY() { return this.posY; }
    public boolean isPositionned() { return this.positionned; }
}