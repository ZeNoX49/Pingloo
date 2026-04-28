package com.dbeditor.model;

public abstract class Attribut {
    public String name;
    public boolean isPrimaryKey;
    public boolean isNotNull;
    public boolean isUnique;

    public Attribut(String name) {
        this.name = name;
        this.isPrimaryKey = false;
        this.isNotNull = false;
        this.isUnique = false;
    }

    public Attribut(Attribut other) {
        this.name = other.name;
        this.isPrimaryKey = other.isPrimaryKey;
        this.isNotNull = other.isNotNull;
        this.isUnique = other.isUnique;
    }
}
