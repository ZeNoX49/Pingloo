package com.dbeditor.model;

import com.dbeditor.model.type.__SqlType;

public class Column extends Attribut {
    public __SqlType type;
    public boolean isAutoIncrementing;
    
    public Column(String name, __SqlType type) {
        super(name);
        this.type = type;
        this.isAutoIncrementing = false;
    }
    
    public Column(Column other) {
        super(other);
        this.type = other.type;
        this.isAutoIncrementing = other.isAutoIncrementing;
    }
}