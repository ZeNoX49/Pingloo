package com.dbeditor.model;

import java.util.LinkedHashMap;

public class ConceptualNode {
    public String name;
    public final LinkedHashMap<String, Column> columns;
    
    public ConceptualNode(String name) {
        this.name = name;
        this.columns = new LinkedHashMap<>();
    }

    public ConceptualNode(ConceptualNode other) {
        this(other.name);
        
        for(Column c : other.columns.values()) {
            this.columns.put(c.name, new Column(c));
        }

        if(other.isPositionned()) {
            this.setPosition(other.getPosX(), other.getPosY());
        }
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