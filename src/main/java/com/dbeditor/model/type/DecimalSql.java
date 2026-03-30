package com.dbeditor.model.type;

import com.dbeditor.sql.DbType;

public class DecimalSql extends SqlType {

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
    public boolean isConform(String data) {
        // TODO
        return false;
    }

    @Override
    public String getRepr(DbType dbType) {
        // TODO
        switch (dbType) {
            case MySql : return "DECIMAL(%d, %d)".formatted(this.before, this.after);
            case MsSql : return "";
            case PostgreSql : return "";
            case Oracle : return "";        
            default: return null;
        }
    }
}
