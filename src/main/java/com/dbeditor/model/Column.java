package com.dbeditor.model;

public class Column {
    private String name;
    private String type;
    private boolean isPrimaryKey;
    private boolean isNotNull;
    private boolean isUnique;
    
    public Column(String name, String type) {
        this.name = name;
        this.type = type;
    }
    
    public Column(Column other) {
        this.name = other.name;
        this.type = other.type;
        this.isPrimaryKey = other.isPrimaryKey;
        this.isNotNull = other.isNotNull;
        this.isUnique = other.isUnique;
    }
    
    public String getName() { return name; }
    public String getType() { return type; }
    public boolean isPrimaryKey() { return isPrimaryKey; }
    public boolean isNotNull() { return isNotNull; }
    public boolean isUnique() { return isUnique; }
    
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setPrimaryKey(boolean isPrimaryKey) { this.isPrimaryKey = isPrimaryKey; }
    public void setNotNull(boolean isNotNull) { this.isNotNull = isNotNull; }
    public void setUnique(boolean isUnique) { this.isUnique = isUnique; }
}