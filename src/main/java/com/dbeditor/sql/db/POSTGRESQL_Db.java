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

public class POSTGRESQL_Db implements SQL_Db {
    private static final Logger LOGGER = Logger.getLogger(POSTGRESQL_Db.class.getName());

    private String dbHost;
    private String dbUser;
    private String dbPassword;
    private String dbPort;

    private Connection connection;

    public POSTGRESQL_Db(String dbHost, String dbUser, String dbPassword, String dbPort) {
        this.dbHost = dbHost;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.dbPort = dbPort;
    }

    /**
     * Connecte au serveur PostgreSQL et sélectionne la base passée en paramètre.
     * URL JDBC PostgreSQL : jdbc:postgresql://host:port/dbname
     */
    public void connect(String dbName) {
        if (isConnected()) {
            LOGGER.info("Déjà connecté.");
            return;
        }

        String url = String.format("jdbc:postgresql://%s:%s/%s", this.dbHost, this.dbPort, dbName);

        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(url, this.dbUser, this.dbPassword);
            this.connection.setAutoCommit(true);
            LOGGER.info("Connexion PostgreSQL établie vers " + url);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Driver JDBC PostgreSQL introuvable. Ajoute le driver 'org.postgresql:postgresql' au classpath.", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Impossible de se connecter à la base PostgreSQL", e);
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
            String catalog = this.connection.getCatalog(); // Should be the DB name

            // Récupère toutes les tables utilisateur (on filtre les schémas système)
            try (ResultSet tablesRs = meta.getTables(catalog, null, "%", new String[]{"TABLE"})) {
                while (tablesRs.next()) {
                    String tableName = tablesRs.getString("TABLE_NAME");
                    String tableSchema = tablesRs.getString("TABLE_SCHEM"); // ex: public, pg_catalog, information_schema

                    if (tableName == null || tableName.trim().isEmpty()) continue;
                    // Ignore les schémas systèmes de Postgres
                    if (tableSchema != null && (tableSchema.startsWith("pg_") || "information_schema".equalsIgnoreCase(tableSchema))) {
                        continue;
                    }

                    Table table = buildTable(meta, catalog, tableSchema, tableName);
                    schema.addTable(table);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur pendant la lecture du schema PostgreSQL", e);
        }

        return schema;
    }

    /**
     * Construit un objet Table en utilisant DatabaseMetaData (colonnes, PK, FK).
     * Pour Postgres on récupère aussi la valeur DEFAULT afin de détecter les serial/nextval -> auto-increment.
     */
    private Table buildTable(DatabaseMetaData meta, String catalog, String schemaPattern, String tableName) throws SQLException {
        Table table = new Table(tableName);

        // Primary keys
        Set<String> primaryKeys = new HashSet<>();
        try (ResultSet pkRs = meta.getPrimaryKeys(catalog, schemaPattern, tableName)) {
            while (pkRs.next()) {
                String colName = pkRs.getString("COLUMN_NAME");
                if (colName != null) primaryKeys.add(colName);
            }
        }

        // Colonnes
        try (ResultSet cols = meta.getColumns(catalog, schemaPattern, tableName, null)) {
            while (cols.next()) {
                String colName = cols.getString("COLUMN_NAME");
                String typeName = cols.getString("TYPE_NAME");
                int columnSize = cols.getInt("COLUMN_SIZE");
                String isNullable = cols.getString("IS_NULLABLE"); // "YES" / "NO"
                String isAuto = null;
                try {
                    isAuto = cols.getString("IS_AUTOINCREMENT"); // selon pilote
                } catch (SQLException ignore) {}

                // colonne par défaut (utile pour détecter nextval('seq'::regclass))
                String columnDef = null;
                try {
                    columnDef = cols.getString("COLUMN_DEF");
                } catch (SQLException ignore) {}

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

                // Détection auto-increment : soit IS_AUTOINCREMENT = YES soit default contient nextval(
                if ("YES".equalsIgnoreCase(isAuto) || (columnDef != null && columnDef.toLowerCase().contains("nextval("))) {
                    column.setAutoIncrementing(true);
                }

                table.addColumn(column);
            }
        }

        // Foreign keys (imported keys)
        try (ResultSet fkRs = meta.getImportedKeys(catalog, schemaPattern, tableName)) {
            while (fkRs.next()) {
                String fkName = fkRs.getString("FK_NAME");
                String fkColumn = fkRs.getString("FKCOLUMN_NAME");
                String pkTable = fkRs.getString("PKTABLE_NAME");
                String pkColumn = fkRs.getString("PKCOLUMN_NAME");
                String pkSchema = fkRs.getString("PKTABLE_SCHEM"); // schéma référencé

                if (fkColumn == null || pkTable == null) continue;

                String referencedTableName = (pkSchema != null && !pkSchema.isEmpty()) ? pkSchema + "." + pkTable : pkTable;

                table.addForeignKey(new ForeignKey(
                    fkName != null ? fkName : (tableName + "_" + fkColumn + "_fk"),
                    fkColumn,
                    referencedTableName,
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
     *
     * Ce parser traite :
     *  - les commentaires -- et /* ... *\/ (les commentaires multi-lignes sont retirés en pré-traitement)
     *  - les littéraux '...' (les ; à l'intérieur ne terminent pas une instruction)
     *  - les dollar-quoted strings $tag$ ... $tag$ (important pour les fonctions PL/pgSQL)
     *
     * Retourne true si tout s'est bien passé.
     */
    public boolean executeSqlScript(String sqlScript) {
        if (!isConnected()) {
            LOGGER.warning("executeSqlScript: pas de connexion active.");
            return false;
        }

        // Supprime les commentaires multi-lignes /* ... */
        sqlScript = sqlScript.replaceAll("(?s)/\\*.*?\\*/", " ");
        sqlScript = sqlScript.replace("\r\n", "\n").replace("\r", "\n");

        List<String> statements = new ArrayList<>();
        // StringBuilder current = new StringBuilder();

        // Parser caractère par caractère pour gérer correctement ' " et $tag$
        try (BufferedReader br = new BufferedReader(new StringReader(sqlScript))) {
            int ch;
            boolean inSingleQuote = false;
            boolean inDoubleQuote = false;
            boolean inLineComment = false;
            String dollarTag = null; // si non-null: on est dans un dollar-quote $tag$
            // Deque<Integer> prevChars = new ArrayDeque<>(2); // pour gérer lookbehind (ex: --)
            StringBuilder buffer = new StringBuilder(); // pour construire la ligne/statement

            while ((ch = br.read()) != -1) {
                char c = (char) ch;
                buffer.append(c);

                // gestion des commentaires en ligne (-- jusqu'à la fin de ligne)
                if (inLineComment) {
                    if (c == '\n') {
                        inLineComment = false;
                        // on garde le newline dans buffer (important pour détection de fin de statement)
                    } else {
                        // ignore le contenu du commentaire (mais on le laisse dans buffer ; on le filtrera ensuite)
                        continue;
                    }
                } else if (dollarTag != null) {
                    // on est dans un dollar-quoted string, cherchons la fin $tag$
                    int tagLen = dollarTag.length();
                    // si buffer se termine par dollarTag, on quitte
                    if (buffer.length() >= tagLen) {
                        String tail = buffer.substring(buffer.length() - tagLen);
                        if (tail.equals(dollarTag)) {
                            // sortie du dollar-quote
                            dollarTag = null;
                        }
                    }
                    // ajoute le caractère et continue
                    continue;
                } else if (inSingleQuote) {
                    if (c == '\'') {
                        // check for escaped single quote by doubling ('')
                        br.mark(1);
                        int next = br.read();
                        if (next == '\'') {
                            // escaped quote, include it and remain in single quote
                            buffer.append('\'');
                            continue;
                        } else if (next != -1) {
                            // not an escaped quote, roll back and end single quote
                            br.reset();
                        }
                        inSingleQuote = false;
                    }
                    continue;
                } else if (inDoubleQuote) {
                    if (c == '"') {
                        inDoubleQuote = false;
                    }
                    continue;
                } else {
                    // pas dans de quote ni commentaire
                    // détecter début de commentaire en ligne
                    if (c == '-') {
                        // regarde le précédent caractère dans buffer (s'il existe)
                        int len = buffer.length();
                        if (len >= 2 && buffer.charAt(len - 2) == '-') {
                            // on vient d'avoir "--"
                            inLineComment = true;
                            // on supprime ces deux tirets du buffer pour ne pas les propager
                            // (on peut aussi les garder ; ils seront ignorés ensuite)
                            continue;
                        }
                    } else if (c == '\'') {
                        inSingleQuote = true;
                        continue;
                    } else if (c == '"') {
                        inDoubleQuote = true;
                        continue;
                    } else if (c == '$') {
                        // possible début de $tag$ ; il faut lire jusqu'au prochain $ pour capturer le tag
                        // on regarde le rest of stream to build tag (peek)
                        br.mark(1000); // marque large
                        StringBuilder tagBuilder = new StringBuilder();
                        tagBuilder.append('$');
                        int nextChar;
                        boolean validTag = false;
                        while ((nextChar = br.read()) != -1) {
                            char nc = (char) nextChar;
                            tagBuilder.append(nc);
                            if (nc == '$') {
                                validTag = true;
                                break;
                            }
                            // limite la taille du tag raisonnablement
                            if (tagBuilder.length() > 100) break;
                        }
                        if (validTag) {
                            // début d'un dollar-quote
                            dollarTag = tagBuilder.toString(); // ex: $$ ou $tag$
                            // ajoute le tag dans buffer
                            buffer.append(tagBuilder.substring(1)); // on avait déjà ajouté first '$'
                            continue;
                        } else {
                            // pas un dollar-tag, on revient en arrière
                            br.reset();
                        }
                    } else if (c == ';') {
                        // fin d'un statement (on n'est pas dans de quote)
                        // on récupère la requête courante
                        String stmt = buffer.toString().trim();
                        if (!stmt.isEmpty()) {
                            // retire le point-virgule final
                            if (stmt.endsWith(";")) {
                                stmt = stmt.substring(0, stmt.length() - 1).trim();
                            }
                            if (!stmt.isEmpty()) statements.add(stmt);
                        }
                        buffer.setLength(0);
                        continue;
                    }
                }
            } // fin lecture

            // ce qui reste dans buffer après la boucle
            String leftover = buffer.toString().trim();
            if (!leftover.isEmpty()) {
                statements.add(leftover);
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
                } catch (SQLException ignore) {}
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