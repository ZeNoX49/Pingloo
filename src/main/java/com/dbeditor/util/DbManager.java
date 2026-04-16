package com.dbeditor.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;
import com.dbeditor.sql.DbType;
import com.dbeditor.sql.db.MySqlDb;
import com.dbeditor.sql.db.SqlDb;
import com.dbeditor.sql.file.exporter.MsSqlExporter;
import com.dbeditor.sql.file.exporter.MySqlExporter;
import com.dbeditor.sql.file.exporter.OracleExporter;
import com.dbeditor.sql.file.exporter.PostgreSqlExporter;
import com.dbeditor.sql.file.exporter.SqlExporter;
import com.dbeditor.sql.file.parser.MsSqlParser;
import com.dbeditor.sql.file.parser.MySqlParser;
import com.dbeditor.sql.file.parser.OracleParser;
import com.dbeditor.sql.file.parser.PostgreSqlParser;
import com.dbeditor.sql.file.parser.SqlParser;

public class DbManager {
    private static DbManager instance;
    public static DbManager getInstance() {
        if(instance == null) {
            instance = new DbManager();
        }
        return instance;
    }

    /* ================================================== */

    private final Map<DbType, SqlDb> sqlDb;
    private final Map<DbType, SqlParser> sqlParser;
    private final Map<DbType, SqlExporter> sqlExporter;
    private final Map<DbType, List<String>> sqlTypeDatabases;
    // private Map<String, SQL_DbExporter> dbExporters;

    private DbManager() {
        this.sqlDb = new HashMap<>();

        this.sqlParser = new HashMap<>();
        this.sqlParser.put(DbType.MySql, new MySqlParser());
        this.sqlParser.put(DbType.MsSql, new MsSqlParser());
        this.sqlParser.put(DbType.PostgreSql, new PostgreSqlParser());
        this.sqlParser.put(DbType.Oracle, new OracleParser());

        this.sqlExporter = new HashMap<>();
        this.sqlExporter.put(DbType.MySql, new MySqlExporter());
        this.sqlExporter.put(DbType.MsSql, new MsSqlExporter());
        this.sqlExporter.put(DbType.PostgreSql, new PostgreSqlExporter());
        this.sqlExporter.put(DbType.Oracle, new OracleExporter());

        this.sqlTypeDatabases = new HashMap<>();
        for(DbType type : DbType.values()) {
            this.sqlTypeDatabases.put(type, new ArrayList<>());
        }
    }

    public void setMysqlDbData(Map<String, Object> data) {
        this.sqlDb.put(DbType.MySql, new MySqlDb(
            (String) data.get("host"),
            (String) data.get("user"),
            (String) data.get("password"),
            (String) data.get("port")
        ));
        
        List<String> t = new ArrayList<>();
        for(Map<String, Object> table : (List<Map<String, Object>>) data.get("databases")) {
            t.add((String) table.get("name"));
        }
        this.sqlTypeDatabases.put(DbType.MySql, t);
    }

    public SqlDb getSqlDb(DbType type) { return this.sqlDb.get(type); }
    public SqlParser getSqlParser(DbType type) { return this.sqlParser.get(type); }
    public SqlExporter getSqlExporter(DbType type) { return this.sqlExporter.get(type); }
    public List<String> getSqlTypeDatabases(DbType type) { return this.sqlTypeDatabases.get(type);  }

    /* ============================================================================================================================= */

    /**
     * Permet de trier les tables dans l'ordre de création pour la bdd
     * Renvoie une erreur si il y a un problème
     * @param tables
     * @return
     */
    public static List<Table> sortTables(Collection<Table> tables) {
        List<Table> res = new ArrayList<>();
        List<Table> toAdd = new ArrayList<>(tables);

        // 1 - On ajoute toutes les tables sans FK
        Iterator<Table> it = toAdd.iterator();
        while(it.hasNext()) {
            Table t = it.next();
            if(t.getForeignKeys().isEmpty()) {
                res.add(t);
                it.remove();
            }
        }

        // 2 - On ajoute les tables dont toutes les FK sont déjà dans res
        boolean added;
        do {
            added = false;
            it = toAdd.iterator();
            while(it.hasNext()) {
                Table t = it.next();
                boolean allFKResolved = true;

                for(ForeignKey fk : t.getForeignKeys()) {
                    String refTable = fk.referencedTable;

                    // si c'est notre table actuelle : on passe
                    if(refTable.equals(t.name)) continue;

                    boolean fkFound = false;
                    for(Table r : res) {
                        if(refTable.equals(r.name)) {

                            fkFound = true;
                            break;
                        }
                    }

                    if(!fkFound) {
                        allFKResolved = false;
                        break;
                    }
                }

                if(allFKResolved) {
                    res.add(t);
                    it.remove();
                    added = true;
                }
            }

            // Si aucune table n'a été ajoutée, on a une FK circulaire
            if(!added && !toAdd.isEmpty()) {
                throw new IllegalStateException("FK circulaire détectée : " + toAdd);
            }

        } while(!toAdd.isEmpty());

        return res;
    }
}