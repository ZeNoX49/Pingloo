package com.dbeditor.sql.db;

import com.dbeditor.model.DatabaseSchema;

public interface SQL_Db {

    /**
     * Renvoie les schema de la bdd chargés à partir de la bdd
     * @param dbName nom de la bdd
     */
    public DatabaseSchema loadDb(String dbName);
    
}