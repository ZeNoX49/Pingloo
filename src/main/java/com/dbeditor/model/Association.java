package com.dbeditor.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Association extends Entity {
    public final Map<String, ForeignKey> foreignKeys;
    
    public Association(String name) {
        super(name);
        this.foreignKeys = new LinkedHashMap<>();
    }

    public Association(Association other) {
        super(other);

        this.foreignKeys = new LinkedHashMap<>();
        for(ForeignKey fk : other.foreignKeys.values()) {
            this.foreignKeys.put(fk.columnName, new ForeignKey(fk));
        }
    }

}