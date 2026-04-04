package com.dbeditor.model.type;

import com.dbeditor.sql.DbType;

public class VarcharSql extends __SqlType implements _OneModifier {

    private int size;

    /**
     * @param size > 0
     */
    public VarcharSql(int size) {
        if (size <= 0) return;
        this.size = size;
    }

    @Override
    public void updateData(int data) {
        if (data <= 0) return;
        this.size = data;
    }

    @Override
    public boolean isConform(String data) {
        // TODO
        return false;
    }

    @Override
    public String getRepr(DbType dbType) {
        // TODO
        return switch (dbType) {
            case MySql -> "VARCHAR(%d)".formatted(this.size);
            case MsSql -> "";
            case PostgreSql -> "";
            case Oracle -> "";
            default -> null;
        };
    }
}
