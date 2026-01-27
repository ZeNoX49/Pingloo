package com.dbeditor.sql.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dbeditor.model.Column;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;

public class MSSQL_Db implements SQL_Db {
    private static final Logger LOGGER = Logger.getLogger(MSSQL_Db.class.getName());

    private String dbHost;
    private String dbUser;
    private String dbPassword;
    private String dbPort;

    private Connection connection;

    public MSSQL_Db(String dbHost, String dbUser, String dbPassword, String dbPort) {
        this.dbHost = dbHost;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.dbPort = dbPort;
    }

    /**
     * Connecte au serveur MSSQL et sélectionne la base passée en paramètre.
     * URL JDBC MSSQL : jdbc:sqlserver://host:port;databaseName=DB;...
     */
    public void connect(String dbName) {
        if (isConnected()) {
            LOGGER.info("Déjà connecté.");
            return;
        }

        // Paramètres recommandés : encrypt et trustServerCertificate selon ton infra
        String url = String.format(
            "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=false;trustServerCertificate=true",
            this.dbHost, this.dbPort, dbName);

        try {
            // Driver Microsoft JDBC pour SQL Server
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            this.connection = DriverManager.getConnection(url, this.dbUser, this.dbPassword);
            this.connection.setAutoCommit(true);
            LOGGER.info("Connexion MSSQL établie vers " + url);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Driver JDBC MSSQL introuvable. Ajoute le driver 'mssql-jdbc' au classpath.", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Impossible de se connecter à la base MSSQL", e);
        }
    }

    public void deconnect() {
        if (this.connection == null) return;
        try {
            if (!this.connection.isClosed()) {
                this.connection.close();
                LOGGER.info("Connexion fermée.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erreur lors de la fermeture de la connexion", e);
        } finally {
            this.connection = null;
        }
    }

    private boolean isConnected() {
        try {
            return this.connection != null && !this.connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public DatabaseSchema loadDb(String dbName) {
        connect(dbName);
        if (!isConnected()) {
            LOGGER.severe("Impossible de charger la BD : pas de connexion.");
            return new DatabaseSchema(dbName);
        }

        DatabaseSchema schema = new DatabaseSchema(dbName);

        try {
            DatabaseMetaData meta = this.connection.getMetaData();
            String catalog = this.connection.getCatalog(); // devrait être le nom de la base

            try (ResultSet tablesRs = meta.getTables(catalog, null, "%", new String[]{"TABLE"})) {
                while (tablesRs.next()) {
                    String tableName = tablesRs.getString("TABLE_NAME");
                    if (tableName == null || tableName.trim().isEmpty()) continue;
                    Table table = buildTable(meta, catalog, tableName);
                    schema.addTable(table);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur pendant la lecture du schema MSSQL", e);
        }

        return schema;
    }

    /**
     * Construit un objet Table en utilisant DatabaseMetaData (colonnes, PK, FK).
     */
    private Table buildTable(DatabaseMetaData meta, String catalog, String tableName) throws SQLException {
        Table table = new Table(tableName);

        // Primary keys
        Set<String> primaryKeys = new HashSet<>();
        try (ResultSet pkRs = meta.getPrimaryKeys(catalog, null, tableName)) {
            while (pkRs.next()) {
                String colName = pkRs.getString("COLUMN_NAME");
                if (colName != null) primaryKeys.add(colName);
            }
        }

        // Colonnes
        try (ResultSet cols = meta.getColumns(catalog, null, tableName, null)) {
            while (cols.next()) {
                String colName = cols.getString("COLUMN_NAME");
                String typeName = cols.getString("TYPE_NAME");
                int columnSize = cols.getInt("COLUMN_SIZE");
                String isNullable = cols.getString("IS_NULLABLE"); // "YES" / "NO"
                String isAuto = null;
                try {
                    isAuto = cols.getString("IS_AUTOINCREMENT"); // peut exister selon pilote
                } catch (SQLException ignore) {
                }

                String fullType = typeName;
                if (columnSize > 0) {
                    fullType += "(" + columnSize + ")";
                }

                Column column = new Column(colName, fullType);
                if ("NO".equalsIgnoreCase(isNullable)) {
                    column.setNotNull(true);
                }
                if (primaryKeys.contains(colName)) {
                    column.setPrimaryKey(true);
                }
                if ("YES".equalsIgnoreCase(isAuto)) {
                    column.setAutoIncrementing(true);
                }

                table.addColumn(column);
            }
        }

        // Foreign keys (imported keys)
        try (ResultSet fkRs = meta.getImportedKeys(catalog, null, tableName)) {
            while (fkRs.next()) {
                String fkName = fkRs.getString("FK_NAME");
                String fkColumn = fkRs.getString("FKCOLUMN_NAME");
                String pkTable = fkRs.getString("PKTABLE_NAME");
                String pkColumn = fkRs.getString("PKCOLUMN_NAME");

                if (fkColumn == null || pkTable == null) continue;

                table.addForeignKey(new ForeignKey(
                    fkName != null ? fkName : (tableName + "_" + fkColumn + "_fk"),
                    fkColumn,
                    pkTable,
                    pkColumn
                ));
            }
        }

        return table;
    }

    /**
     * Exécute une requête SELECT et retourne les lignes sous forme de liste de maps.
     */
    public List<Map<String, Object>> queryForList(String query) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (!isConnected()) {
            LOGGER.warning("queryForList: pas de connexion active.");
            return rows;
        }

        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData md = rs.getMetaData();
            int colCount = md.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    String colName = md.getColumnLabel(i);
                    Object value = rs.getObject(i);
                    row.put(colName, value);
                }
                rows.add(row);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL dans queryForList", e);
        }

        return rows;
    }

    /**
     * Exécute un script SQL en découpant correctement les statements.
     * Supporte : 
     *  - point-virgule ';' comme séparateur classique,
     *  - la directive 'GO' (ligne seule) très utilisée par SQL Server.
     *
     * Retourne true si tout s'est bien passé.
     */
    public boolean executeSqlScript(String sqlScript) {
        if (!isConnected()) {
            LOGGER.warning("executeSqlScript: pas de connexion active.");
            return false;
        }

        // retirer commentaires multi-lignes /* ... */
        sqlScript = sqlScript.replaceAll("(?s)/\\*.*?\\*/", " ");
        sqlScript = sqlScript.replace("\r\n", "\n").replace("\r", "\n");

        List<String> statements = new ArrayList<>();
        String delimiter = ";";
        StringBuilder current = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new StringReader(sqlScript))) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                if (trimmed.startsWith("--") || trimmed.startsWith("#")) continue;
                if (trimmed.toUpperCase(Locale.ROOT).startsWith("USE ")) continue;

                // Support pour DELIMITER si utilisé (rare sur MSSQL, mais pas gênant)
                if (trimmed.toUpperCase(Locale.ROOT).startsWith("DELIMITER ")) {
                    delimiter = trimmed.substring("DELIMITER ".length());
                    continue;
                }

                // Support pour 'GO' sur une ligne (séparateur de batch MSSQL / sqlcmd / SSMS)
                if (trimmed.equalsIgnoreCase("GO")) {
                    String stmt = current.toString().trim();
                    if (!stmt.isEmpty()) statements.add(stmt);
                    current.setLength(0);
                    continue;
                }

                current.append(line).append("\n");
                String curStr = current.toString();

                if (delimiter.length() == 1) {
                    if (curStr.trim().endsWith(delimiter)) {
                        String stmt = curStr.trim();
                        stmt = stmt.substring(0, stmt.length() - delimiter.length()).trim();
                        if (!stmt.isEmpty()) statements.add(stmt);
                        current.setLength(0);
                    }
                } else {
                    if (curStr.endsWith(delimiter + "\n") || curStr.endsWith(delimiter)) {
                        int idx = curStr.lastIndexOf(delimiter);
                        String stmt = (idx >= 0) ? curStr.substring(0, idx).trim() : curStr.trim();
                        if (!stmt.isEmpty()) statements.add(stmt);
                        current.setLength(0);
                    }
                }
            }

            if (current.length() > 0) {
                String leftover = current.toString().trim();
                if (!leftover.isEmpty()) statements.add(leftover);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lecture du script SQL", e);
            return false;
        }

        if (statements.isEmpty()) {
            LOGGER.info("Aucun statement trouvé dans le script.");
            return true;
        }

        boolean success = true;
        try (Statement stmt = this.connection.createStatement()) {
            boolean prevAutoCommit = this.connection.getAutoCommit();
            try {
                this.connection.setAutoCommit(false);
                for (String s : statements) {
                    if (s == null || s.trim().isEmpty()) continue;
                    stmt.execute(s);
                }
                this.connection.commit();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de l'exécution du script SQL", e);
                success = false;
                try {
                    this.connection.rollback();
                    LOGGER.warning("Transaction annulée (rollback).");
                } catch (SQLException rbe) {
                    LOGGER.log(Level.SEVERE, "Rollback a échoué", rbe);
                }
            } finally {
                try {
                    this.connection.setAutoCommit(prevAutoCommit);
                } catch (SQLException ignore) { }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur sur Statement lors du script", e);
            success = false;
        }

        if (success) LOGGER.info("Script exécuté avec succès.");
        return success;
    }

    /* getters / setters */
    public void setDbHost(String dbHost) { this.dbHost = dbHost; }
    public void setDbUser(String dbUser) { this.dbUser = dbUser; }
    public void setDbPassword(String dbPassword) { this.dbPassword = dbPassword; }
    public void setDbPort(String dbPort) { this.dbPort = dbPort; }

    public String getDbHost() { return this.dbHost; }
    public String getDbUser() { return this.dbUser; }
    public String getDbPassword() { return this.dbPassword; }
    public String getDbPort() { return this.dbPort; }
}