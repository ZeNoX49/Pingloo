package com.dbeditor.model;

public class Column {
    private String name;
    private String type;
    private boolean isPrimaryKey;
    private boolean isNotNull;
    private boolean isUnique;
    private boolean isAutoIncrementing;
    
    public Column(String name, String type) {
        this.name = name;
        this.type = type.replace(" ", "");
    }
    
    public Column(Column other) {
        this.name = other.name;
        this.type = other.type;
        this.isPrimaryKey = other.isPrimaryKey;
        this.isNotNull = other.isNotNull;
        this.isUnique = other.isUnique;
        this.isAutoIncrementing = other.isAutoIncrementing;
    }
    
    public String getName() { return name; }
    public String getType() { return type; }

    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }


    public boolean isPrimaryKey() { return this.isPrimaryKey; }
    public boolean isNotNull() { return this.isNotNull; }
    public boolean isUnique() { return this.isUnique; }
    public boolean isAutoIncrementing() { return this.isAutoIncrementing; }

    public void setPrimaryKey(boolean bool) { this.isPrimaryKey = bool; }
    public void setNotNull(boolean bool) { this.isNotNull = bool; }
    public void setUnique(boolean bool) { this.isUnique = bool; }
    public void setAutoIncrementing(boolean bool) { this.isAutoIncrementing = bool; }
}