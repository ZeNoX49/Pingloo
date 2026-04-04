package com.dbeditor.model;

import com.dbeditor.model.type.__SqlType;

public class Column {
    public String name;
    public __SqlType type;
    public boolean isPrimaryKey;
    public boolean isNotNull;
    public boolean isUnique;
    public boolean isAutoIncrementing;
    
    public Column(String name, __SqlType type) {
        this.name = name;
        this.type = type;
    }
    
    public Column(Column other) {
        this.name = other.name;
        this.type = other.type;
        this.isPrimaryKey = other.isPrimaryKey;
        this.isNotNull = other.isNotNull;
        this.isUnique = other.isUnique;
        this.isAutoIncrementing = other.isAutoIncrementing;
    }
}