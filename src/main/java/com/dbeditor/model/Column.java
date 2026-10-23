package com.dbeditor.model;

public class Column extends Attribut {

    // public __SqlType type;
    public String type;
    public boolean isAutoIncrementing;
    // public final DataDictionnary dataDictionnary;

    public Column(String name, String type) {
        super(name);
        this.type = type;
        this.isAutoIncrementing = false;
        // this.dataDictionnary = new DataDictionnary("", DataDictionnary.DD_Type.AN, DataDictionnary.DD_Nature.E);
    }

    // public Column(Column other) {
    //     super(other);
    //     this.type = other.type;
    //     this.isAutoIncrementing = other.isAutoIncrementing;
    //     // this.dataDictionnary = other.dataDictionnary;
    // }
}
