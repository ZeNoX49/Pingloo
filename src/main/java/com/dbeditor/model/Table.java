package com.dbeditor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Table {
    public String name;
    private final LinkedHashMap<String, Boolean> attributs; // si true -> colonne
    public final Map<String, Column> columns;
    public final Map<String, ForeignKey> foreignKeys;
    
    public Table(String name) {
        this.name = name;
        this.attributs = new LinkedHashMap<>();
        this.columns = new HashMap<>();
        this.foreignKeys = new HashMap<>();
    }

    public Table(Table other) {
        this(other.name);
        for(Entry<String, Boolean> entry : other.attributs.entrySet()) {
            if(entry.getValue()) {
                this.addColumn(new Column(other.columns.get(entry.getKey())));
            } else {
                this.addForeignKey(new ForeignKey(other.foreignKeys.get(entry.getKey())));
            }
        }
        if(other.positionned) this.setPosition(other.posX, other.posY);
    }

    public final void addColumn(Column col) {
        this.columns.put(col.name, col);
        this.attributs.put(col.name, true);
    }
    public List<Column> getColumns() {
        return new ArrayList<>(this.columns.values());
    }

    public final void addForeignKey(ForeignKey fk) {
        this.foreignKeys.put(fk.name, fk);
        // la colonne sera (surement) déja référencé, donc on l'enlève
        Column col = this.columns.remove(fk.name);
        if (col != null) {
            fk.isPrimaryKey = col.isPrimaryKey;
        }
        this.attributs.put(fk.name, false);
    }
    public List<ForeignKey> getForeignKeys() {
        return new ArrayList<>(this.foreignKeys.values());
    }

    /**
     * si true -> colonne
     */
    public LinkedHashMap<String, Boolean> getAttributs() {
        return new LinkedHashMap<>(this.attributs);
    }

    /* =================================================================== */
    // pour que les tables aient la même position sur un changement de vue //
    /* =================================================================== */
    private double posX = 0;
    private double posY = 0;
    private boolean positionned = false;

    public final void setPosition(double x, double y) {
        this.posX = x;
        this.posY = y;
        this.positionned = true;
    }

    public double getPosX() { return this.posX; }
    public double getPosY() { return this.posY; }
    public boolean isPositionned() { return this.positionned; }
}