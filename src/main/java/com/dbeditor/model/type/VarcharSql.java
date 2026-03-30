package com.dbeditor.model.type;

import com.dbeditor.sql.DbType;

public class VarcharSql extends SqlType {

    private int size;

    /**
     * @param size > 0
     */
    public VarcharSql(int size) {
        if (size <= 0) return;
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
