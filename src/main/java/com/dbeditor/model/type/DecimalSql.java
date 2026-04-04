package com.dbeditor.model.type;

import com.dbeditor.sql.DbType;

public class DecimalSql extends __SqlType implements _TwoModifier {

    private int before;
    private int after;

    /**
     * @param before > 0
     * @param after > 0
     */
    public DecimalSql(int before, int after) {
        if (before <= 0 || after <= 0) return;
        this.before = before;
        this.after = after;
    }

    @Override
    public void updateData(int data1, int data2) {
        if (data1 <= 0 || data2 <= 0) return;
        this.before = data1;
        this.after = data2;
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
            case MySql -> "DECIMAL(%d, %d)".formatted(this.before, this.after);
            case MsSql -> "";
            case PostgreSql -> "";
            case Oracle -> "";
            default -> null;
        };
    }
}
