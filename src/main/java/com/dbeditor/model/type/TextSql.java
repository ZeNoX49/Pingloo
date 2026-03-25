package com.dbeditor.model.type;

import com.dbeditor.sql.DbType;

public class TextSql extends SqlType {

    @Override
    public boolean isConform(String data) {
        // TODO
        return false;
    }

    @Override
    public String getRepr(DbType dbType) {
        // TODO
        switch (dbType) {
            case MySql : return "TEXT";
            case MsSql : return "";
            case PostgreSql : return "";
            case Oracle : return "";        
            default: return null;
        }
    }
}
