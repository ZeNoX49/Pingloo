package com.dbeditor.model;

public abstract class Attribute {

    public String name;
    public boolean isNotNull;
    public boolean isUnique;

    public Attribute(String name) {
        this.name = name;
        this.isNotNull = false;
        this.isUnique = false;
    }

    public Attribute(Attribute other) {
        this.name = other.name;
        this.isNotNull = other.isNotNull;
        this.isUnique = other.isUnique;
    }

}
