package com.dbeditor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Table {

    private static enum TableColumnType {
        COLUMN, FOREIGN_KEY;
    }

    public String name;
    private final LinkedHashMap<String, TableColumnType> attributesOrder;
    public final Map<String, Column> columns;
    public final Map<String, ForeignKey> foreignKeys;

    public Table(String name) {
        this.name = name;
        this.attributesOrder = new LinkedHashMap<>();
        this.columns = new HashMap<>();
        this.foreignKeys = new HashMap<>();
    }

    // public Table(Table other) {
    //     this(other.name);
    //     for (Entry<String, Boolean> entry : other.attributesOrder.entrySet()) {
    //         if (entry.getValue()) {
    //             this.addColumn(new Column(other.columns.get(entry.getKey())));
    //         } else {
    //             this.addForeignKey(new ForeignKey(other.foreignKeys.get(entry.getKey())));
    //         }
    //     }
    //     if (other.positionned) {
    //         this.setPosition(other.posX, other.posY);
    //     }
    // }

    public final void addColumn(Column col) {
        this.columns.put(col.name, col);
        this.attributesOrder.put(col.name, TableColumnType.COLUMN);
    }

    public List<Column> getColumns() {
        List<Column> result = new ArrayList<>();

        for (Map.Entry<String, TableColumnType> entry : this.attributesOrder.entrySet()) {
            if (entry.getValue() == TableColumnType.COLUMN) {
                result.add(columns.get(entry.getKey()));
            }
        }

        return result;
    }

    public final void addForeignKey(ForeignKey fk) {
        this.foreignKeys.put(fk.name, fk);
        // // la colonne sera (surement) déja référencé, donc on l'enlève
        // Column col = this.columns.remove(fk.name);
        // if (col != null) {
        //     fk.isPrimaryKey = col.isPrimaryKey;
        // }
        this.attributesOrder.put(fk.name, TableColumnType.FOREIGN_KEY);
    }

    public List<ForeignKey> getForeignKeys() {
        List<ForeignKey> result = new ArrayList<>();

        for (Map.Entry<String, TableColumnType> entry : this.attributesOrder.entrySet()) {
            if (entry.getValue() == TableColumnType.FOREIGN_KEY) {
                result.add(foreignKeys.get(entry.getKey()));
            }
        }

        return result;
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

    public double getPosX() {
        return this.posX;
    }

    public double getPosY() {
        return this.posY;
    }

    public boolean isPositionned() {
        return this.positionned;
    }
}
