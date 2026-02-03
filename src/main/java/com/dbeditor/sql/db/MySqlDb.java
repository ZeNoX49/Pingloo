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

public class MySqlDb implements SqlDb {
    private static final Logger LOGGER = Logger.getLogger(MySqlDb.class.getName());

    private String dbHost;
    private String dbUser;
    private String dbPassword;
    private String dbPort;

    private Connection connection;

    public MySqlDb(String dbHost, String dbUser, String dbPassword, String dbPort) {
        this.dbHost = dbHost;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.dbPort = dbPort;
    }

    /**
     * Connecte au serveur MySQL et sélectionne la base passée en paramètre.
     * L'URL contient des paramètres recommandés (serverTimezone, allowPublicKeyRetrieval, useSSL).
     */
    public void connect(String dbName) {
        if (isConnected()) {
            LOGGER.info("Déjà connecté.");
            return;
        }

        String url = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
            this.dbHost, this.dbPort, dbName);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, this.dbUser, this.dbPassword);
            this.connection.setAutoCommit(true); // comportement par défaut ; transactionnel au besoin
            LOGGER.info("Connexion établie vers " + url);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Driver JDBC introuvable", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Impossible de se connecter à la base", e);
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
        this.connect(dbName);
        if (!isConnected()) {
            LOGGER.severe("Impossible de charger la BD : pas de connexion.");
            return new DatabaseSchema(dbName);
        }

        DatabaseSchema schema = new DatabaseSchema(dbName);

        try {
            DatabaseMetaData meta = this.connection.getMetaData();
            String catalog = this.connection.getCatalog();

            try (ResultSet tablesRs = meta.getTables(catalog, null, "%", new String[]{"TABLE"})) {
                while (tablesRs.next()) {
                    String tableName = tablesRs.getString("TABLE_NAME");
                    if (tableName == null || tableName.trim().isEmpty()) continue;
                    Table table = this.buildTable(meta, catalog, tableName);
                    schema.addTable(table);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur pendant la lecture du schema", e);
        } finally {
            // on ne ferme pas la connexion ici si tu veux réutiliser l'instance plus tard,
            // mais on peut la fermer si tu préfères. Ici on la laisse ouverte pour l'appelant.
        }

        return schema;
    }

    /**
     * Construit un objet Table en utilisant DatabaseMetaData (colonnes, PK, FK).
     */
    private Table buildTable(DatabaseMetaData meta, String catalog, String tableName) throws SQLException {
        Table table = new Table(tableName);

        // Récupérer les primary keys d'abord pour marquer les colonnes
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
                    // certains pilotes fournissent IS_AUTOINCREMENT
                    isAuto = cols.getString("IS_AUTOINCREMENT");
                } catch (SQLException ignore) {
                    // si absent, on l'ignore
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

        // Foreign keys pour cette table
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
     * Vérifie que la connexion est ouverte.
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
     * Exécute un script SQL en découpant correctement les statements (supporte DELIMITER).
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

                if (trimmed.toUpperCase(Locale.ROOT).startsWith("DELIMITER ")) {
                    delimiter = trimmed.substring("DELIMITER ".length());
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
