package com.dbeditor.model.type;

import com.dbeditor.sql.DbType;

public abstract class SqlType {
    
    public static SqlType get(String type) {
        return null;
    }

    public abstract boolean isConform(String data);
    public abstract String getRepr(DbType dbType);
}
