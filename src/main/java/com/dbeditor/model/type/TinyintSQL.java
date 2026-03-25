package com.dbeditor.model.type;

import com.dbeditor.sql.DbType;

public class VarcharSql extends SqlType {

    private int size;

    public VarcharSql(int size) {
        this.size = size;
    }

    @Override
    public boolean isConform(String data) {
        // TODO
        return false;
    }

    @Override
    public String getRepr(DbType dbType) {
        // TODO
        switch (dbType) {
            case MySql : return "VARCHAR(%d)".formatted(this.size);
            case MsSql : return "";
            case PostgreSql : return "";
            case Oracle : return "";        
            default: return null;
        }
    }
}
