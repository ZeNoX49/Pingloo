package com.dbeditor.model;

public abstract class Attribute {

    public String name;
    public boolean isPrimaryKey;
    public boolean isNotNull;
    public boolean isUnique;

    public Attribute(String name) {
        this.name = name;
        this.isPrimaryKey = false;
        this.isNotNull = false;
        this.isUnique = false;
    }

    public Attribute(Attribute other) {
        this.name = other.name;
        this.isPrimaryKey = other.isPrimaryKey;
        this.isNotNull = other.isNotNull;
        this.isUnique = other.isUnique;
    }

}
