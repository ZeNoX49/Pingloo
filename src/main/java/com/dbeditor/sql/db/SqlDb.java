package com.dbeditor.sql.db;

public abstract class SqlDb {

    public String dbHost;
    public String dbUser;
    public String dbPassword;
    public String dbPort;

    /**
     * Renvoie les schema de la bdd chargés à partir de la bdd
     * @param dbName nom de la bdd
     */
    public abstract void loadDb(String dbName);
    
    /**
     * Exécute un script SQL.
     * Retourne true si tout s'est bien passé.
     * @param sqlScript tout le script sql a éxécuté
     */
    public abstract boolean executeSqlScript(String sqlScript);
}