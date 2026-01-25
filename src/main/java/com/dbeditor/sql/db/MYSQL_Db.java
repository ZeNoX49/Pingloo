package com.dbeditor.sql.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dbeditor.model.Column;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;

public class MYSQL_Db implements SQL_Db {
    private String dbHost;
    private String dbUser;
    private String dbdPassword;
    private String dbdPort;

    private Connection connection;

    public MYSQL_Db(String dbHost, String dbUser, String dbdPassword, String dbdPort) {
        this.dbHost = dbHost;
        this.dbUser = dbUser;
        this.dbdPassword = dbdPassword;
        this.dbdPort = dbdPort;
    }

    public void connect(String dbName) {
        String url = "jdbc:mysql://"+ this.dbHost +":"+ this.dbdPort +"/"+ dbName;

        try {
            // Optionnel mais parfois utile
            Class.forName("com.mysql.cj.jdbc.Driver");

            this.connection = DriverManager.getConnection(url, this.dbUser, this.dbdPassword);
            this.connection.setAutoCommit(false);
            System.out.println("\nConnexion réussie !");
        } catch (ClassNotFoundException e) {
            System.err.println("\nDriver JDBC introuvable : " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("\nLe serveur est-il allumé ?");
        }
    }

    public void deconnect() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
                System.out.println("Connexion fermée.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DatabaseSchema loadDb(String dbName) {
        this.connect(dbName);

        DatabaseSchema schema = new DatabaseSchema(dbName);
        
        List<Map<String, Object>> list = this.queryForList("SELECT TABLE_NAME " +
                                                           "FROM INFORMATION_SCHEMA.TABLES " +
                                                           "WHERE TABLE_SCHEMA = '" + dbName + "' " +
                                                           "AND TABLE_TYPE = 'BASE TABLE';");
        for (Map<String, Object> data : list) {
            String tableName = this.getString(data, "TABLE_NAME");
            schema.addTable(this.getTable(dbName, tableName));
        }

        this.setupFk(schema);

        this.deconnect();

        return schema;
    }

    /**
     * permet de créer une table avec tout ce qu'elle possède
     * @param dbName le nom de la bdd
     * @param tName le nom de la table
     * @return la table créer et les foreign key
     */
    private Table getTable(String dbName, String tName) {
        Table table = new Table(tName);
        String query = "SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY, EXTRA " +
                       "FROM INFORMATION_SCHEMA.COLUMNS " +
                       "WHERE TABLE_SCHEMA = '" + dbName + "' AND TABLE_NAME LIKE '" + tName +"' " +
                       "ORDER BY ORDINAL_POSITION;";

        List<Map<String, Object>> list = this.queryForList(query);
        
        boolean hasFk = false;

        for(Map<String, Object> data : list) {
            Column column = new Column(
                this.getString(data, "COLUMN_NAME"), 
                this.getString(data, "COLUMN_TYPE")
            );

            if("NO".equals(this.getString(data, "IS_NULLABLE"))) {
                column.setNotNull(true);
            }

            if("PRI".equals(this.getString(data, "COLUMN_KEY"))) {
                column.setPrimaryKey(true);
            }

            if("auto_increment".equals(this.getString(data, "EXTRA"))) {
                column.setAutoIncrementing(true);
            }

            table.addColumn(column);
        }

        return table;
    }

    private String getString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : "";
    }

    private void setupFk(DatabaseSchema dbS) {
        for(Table t : dbS.getTables().values()) {
            String query = "SELECT " +
                "    TABLE_NAME" +
                "    COLUMN_NAME, " +
                "    CONSTRAINT_NAME, " +
                "    REFERENCED_TABLE_NAME, " +
                "    REFERENCED_COLUMN_NAME " +
                "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                "WHERE TABLE_SCHEMA = '" + dbS.getName() + "' " +
                "AND TABLE_NAME = '" + t.getName() + "' " +
                "AND REFERENCED_TABLE_NAME IS NOT NULL;";

            List<Map<String, Object>> list = this.queryForList(query);

            for(Map<String, Object> data : list) {
                t.addForeignKey(new ForeignKey(
                    this.getString(data, "CONSTRAINT_NAME"),
                    this.getString(data, "COLUMN_NAME"),
                    this.getString(data, "REFERENCED_TABLE_NAME"),
                    this.getString(data, "REFERENCED_COLUMN_NAME")
                ));
            }
        }
    }

    /**
     * Exécute une requête SELECT et retourne les lignes sous forme de liste de maps
     * @param query requête sql
     */
    public List<Map<String, Object>> queryForList(String query) {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Statement stmt = this.connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            int colCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    String colName = rs.getMetaData().getColumnLabel(i);
                    Object value = rs.getObject(i);
                    row.put(colName, value);
                }
                rows.add(row);
            }
            return rows;

        } catch (SQLException e) {
            this.handleException(e);
            return rows;
        }
    }

    /**
     * Exécute un script SQL (fichier) en découpant correctement les statements.
     * Retourne true si tout s'est bien passé, false sinon.
     */
    public boolean executeSqlScript(String query) {
        try {
            // supprime les commentaires /* ... */ multi-lignes
            query = query.replaceAll("(?s)/\\*.*?\\*/", " ");

            // normalise les retours chariots
            query = query.replace("\r\n", "\n").replace("\r", "\n");

            // parser ligne par ligne en supportant DELIMITER
            List<String> statements = new ArrayList<>();
            String delimiter = ";";
            StringBuilder current = new StringBuilder();

            try (BufferedReader br = new BufferedReader(new StringReader(query))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String trimmed = line.trim();

                    if (trimmed.isEmpty()) {
                        continue;                  // ligne vide

                    }
                    if (trimmed.startsWith("--")) {
                        continue;          // commentaire SQL

                    }
                    if (trimmed.startsWith("#")) {
                        continue;           // commentaire SQL

                    }
                    if (trimmed.toUpperCase().startsWith("USE ")) {  // ignore USE
                        continue;
                    }
                    if (trimmed.toUpperCase().startsWith("DELIMITER ")) { // change delimiter
                        delimiter = trimmed.substring("DELIMITER ".length());
                        // assure qu'on passe au suivant
                        continue;
                    }

                    current.append(line).append("\n");

                    // check si la fin correspond au delimiter
                    String curStr = current.toString();
                    if (delimiter.length() == 1) {
                        // cas courant : delimiter = ;
                        if (curStr.trim().endsWith(delimiter)) {
                            String stmt = curStr.trim();
                            // retire le delimiter final
                            stmt = stmt.substring(0, stmt.length() - delimiter.length()).trim();
                            if (!stmt.isEmpty()) {
                                statements.add(stmt);
                            }
                            current.setLength(0);
                        }
                    } else {
                        // delimiter multi-caractères (ex: $$)
                        if (curStr.endsWith(delimiter + "\n") || curStr.endsWith(delimiter)) {
                            String stmt = curStr;
                            int idx = stmt.lastIndexOf(delimiter);
                            if (idx >= 0) {
                                stmt = stmt.substring(0, idx).trim();
                                if (!stmt.isEmpty()) {
                                    statements.add(stmt);
                                }
                            }
                            current.setLength(0);
                        }
                    }
                } // end while

                // si reste quelque chose
                if (current.length() > 0) {
                    String leftover = current.toString().trim();
                    if (!leftover.isEmpty()) {
                        statements.add(leftover);
                    }
                }
            }

            if (statements.isEmpty()) {
                System.out.println("Aucun statement trouvé dans la requête");
                return true;
            }

            // Exécution dans une transaction
            try (Statement stmt = this.connection.createStatement()) {
                this.connection.setAutoCommit(false);
                for (String s : statements) {
                    // skip safety: ignore les lignes vides
                    if (s == null || s.trim().isEmpty()) {
                        continue;
                    }
                    stmt.execute(s);
                }
                this.connection.commit();
                System.out.println("Script exécuté");
                return true;
            } catch (SQLException e) {
                System.err.println("Erreur SQL dans la requête : " + e.getMessage());
                try {
                    if (this.connection != null) {
                        this.connection.rollback();
                        System.err.println("Transaction annulée");
                    }
                } catch (SQLException rollBackException) {
                    rollBackException.printStackTrace();
                }
                return false;
            }

        } catch (IOException e) {
            System.err.println("Erreur lecture de le ctrure de la requête : " + e.getMessage());
            return false;
        }
    }

    /**
     * Permet de gérer les exceptions
     * @param e -> SQLException
     */
    private void handleException(SQLException e) {
        System.err.println("Erreur SQL : " + e.getMessage());
        try {
            if (this.connection != null) {
                this.connection.rollback();
                System.err.println("Transaction annulée");
            }
        } catch (SQLException rollBackException) {
            rollBackException.printStackTrace();
        }
    }

    public void setDbHost(String dbHost) { this.dbHost = dbHost; }
    public void setDbUser(String dbUser) { this.dbUser = dbUser; }
    public void setDbdPassword(String dbdPassword) { this.dbdPassword = dbdPassword; }
    public void setDbdPort(String dbdPort) { this.dbdPort = dbdPort; }

    public String getDbHost() { return this.dbHost; }
    public String getDbUser() { return this.dbUser; }
    public String getDbdPassword() { return this.dbdPassword; }
    public String getDbdPort() { return this.dbdPort; }
}