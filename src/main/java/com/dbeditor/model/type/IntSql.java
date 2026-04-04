package com.dbeditor.model.type;

import com.dbeditor.sql.DbType;

public class IntSql extends __SqlType {

    @Override
    public boolean isConform(String data) {
        // TODO
        return false;
    }

    @Override
    public String getRepr(DbType dbType) {
        // TODO
        return switch (dbType) {
            case MySql -> "INT";
            case MsSql -> "";
            case PostgreSql -> "";
            case Oracle -> "";
            default -> null;
        };
    }
}
