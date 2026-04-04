package com.dbeditor.sql.db;

import com.dbeditor.model.DatabaseSchema;

public class OracleSqlDb extends SqlDb {

    @Override
    public DatabaseSchema loadDb(String dbName) {
        // TODO
        return new DatabaseSchema("");
    }
    
    @Override
    public boolean executeSqlScript(String sqlScript) {
        // TODO
        return false;
    }
}