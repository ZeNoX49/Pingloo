package com.dbeditor.model;

public class Column extends Attribute {

    // TODO: public __SqlType type;
    public String type;
    private boolean primaryKey;
    public boolean isAutoIncrementing;
    // TODO: public final DataDictionnary dataDictionnary;

    public Column(String name, String type) {
        super(name);
        this.type = type;
        this.primaryKey = false;
        this.isAutoIncrementing = false;
        // this.dataDictionnary = new DataDictionnary("", DataDictionnary.DD_Type.AN, DataDictionnary.DD_Nature.E);
    }

    public Column(Column other) {
        super(other);
        this.type = other.type;
        this.primaryKey = other.primaryKey;
        this.isAutoIncrementing = other.isAutoIncrementing;
        // this.dataDictionnary = other.dataDictionnary;
    }

    public void setPrimaryKey(boolean value) {
        this.primaryKey = value;
        if (value) {
            this.isNotNull = true;
            this.isUnique = true;
        }
    }

    public boolean isPrimaryKey() { return this.primaryKey; }

}
