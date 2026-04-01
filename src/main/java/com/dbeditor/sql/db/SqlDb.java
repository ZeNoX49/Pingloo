package com.dbeditor.sql.db;

import com.dbeditor.model.DatabaseSchema;

public abstract class SqlDb {

    public String dbHost;
    public String dbUser;
    public String dbPassword;
    public String dbPort;

    /**
     * Renvoie les schema de la bdd chargés à partir de la bdd
     * @param dbName nom de la bdd
     */
    public abstract DatabaseSchema loadDb(String dbName);
    
    /**
     * 
     */
    public abstract boolean executeSqlScript(String sqlScript);
}