package com.dbeditor.model;

import java.util.ArrayList;
import java.util.List;

public class Table {

    public String name;
    private final List<Attribute> attributes;

    public Table(String name) {
        this.name = name;
        this.attributes = new ArrayList<>();
    }

    public Table(Table other) {
        this(other.name);

        for (Attribute attribute : other.attributes) {
            if (attribute instanceof Column col) {
                this.addColumn(new Column(col));
            } else if (attribute instanceof ForeignKey fk) {
                this.addForeignKey(new ForeignKey(fk));
            }
        }

        if (other.positionned) {
            this.setPosition(other.posX + 10, other.posY + 10);
        }
    }

    public final void addColumn(Column col) {
        this.attributes.add(col);
    }

    public final void addForeignKey(ForeignKey fk) {
        this.attributes.add(fk);
    }

    public List<Column> getColumns() {
        return this.attributes.stream()
                .filter(Column.class::isInstance)
                .map(Column.class::cast)
                .toList();
    }

    public List<ForeignKey> getForeignKeys() {
        return this.attributes.stream()
                .filter(ForeignKey.class::isInstance)
                .map(ForeignKey.class::cast)
                .toList();
    }

    // public List<Attribute> getAttributes() {
    //     return new ArrayList<>(this.attributes);
    // }

    // public void removeAttribute(Attribute attribute) {
    //     this.attributes.remove(attribute);
    // }

    // public void renameAttribute(Attribute attribute, String newName) {
    //     if (this.attributes.contains(attribute)) {
    //         attribute.name = newName;
    //     }
    // }

    public void clearAttributes() {
        this.attributes.clear();
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
