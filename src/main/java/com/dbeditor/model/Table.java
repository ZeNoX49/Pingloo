package com.dbeditor.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Table {
    public String name;
    public final LinkedHashMap<String, Column> columns;
    public final LinkedHashMap<String, ForeignKey> foreignKeys;
    
    public Table(String name) {
        this.name = name;
        this.columns = new LinkedHashMap<>();
        this.foreignKeys = new LinkedHashMap<>();
    }

    public Table(Table other) {
        this(other.name);
        for(Column col : other.columns.values()) this.columns.put(col.name, new Column(col));
        for(ForeignKey fk : other.foreignKeys.values()) this.foreignKeys.put(fk.fkName, new ForeignKey(fk));
    }

    public List<Column> getColumns() {
        return new ArrayList<>(this.columns.values());
    }

    public List<ForeignKey> getForeignKeys() {
        return new ArrayList<>(this.foreignKeys.values());
    }

    // @Override
    // public String toString() {
    //     StringBuilder res = new StringBuilder(this.getName() + "\n");

    //     if(!columns.isEmpty()) {
    //         res.append("column | pk : [\n");
    //         for(Column col : columns.values()) {
    //             res.append("\t" + col.getName() + " | " + col.isPrimaryKey() + "\n");
    //         } res.append("]\n");
    //     }

    //     if(!foreignKeys.isEmpty()) {
    //         res.append("foreign key : [\n");
    //         for(ForeignKey fk : foreignKeys) {
    //             res.append("\t" + fk.getFkName() + "\n");
    //         } res.append("]\n");
    //     }

    //     return res.toString() + "\n";
    // }

    /* =================================================================== */
    // pour que les tables aient la même position sur un changement de vue //
    /* =================================================================== */
    public float posX = 0;
    public float posY = 0;
}