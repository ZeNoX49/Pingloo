package com.dbeditor.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;
import com.dbeditor.sql.db.MySqlDb;
import com.dbeditor.sql.db.SqlDb;

public class DbManager {
    private static DbManager instance;
    public static DbManager getInstance() {
        if(instance == null) {
            instance = new DbManager();
        }
        return instance;
    }

    /* ================================================== */

    private Map<String, SqlDb> db;
    private Map<String, List<String>> tables;
    // private Map<String, SQL_DbExporter> dbExporters;

    private DbManager() {
        this.db = new HashMap<>();
        this.tables = new HashMap<>();
    }

    public void setMysqlDbData(Map<String, Object> data) {
        this.db.put("mysql", new MySqlDb(
            (String) data.get("host"),
            (String) data.get("user"),
            (String) data.get("password"),
            (String) data.get("port")
        ));
        
        List<String> t = new ArrayList<>();
        for(Map<String, Object> table : (List<Map<String, Object>>) data.get("tables")) {
            t.add((String) table.get("name"));
        }
        this.tables.put("mysql", t);
    }

    public MySqlDb getMysqlDb() { return (MySqlDb) this.db.get("mysql");  }
    public List<String> getMysqlDbTables() { return this.tables.get("mysql");  }

    /* ============================================================================================================================= */

    /**
     * Permet de trier les tables dans l'ordre de création pour la bdd
     * Renvoie une erreur si il y a un problème
     * @param tables
     * @return
     */
    public List<Table> sortTables(Collection<Table> tables) {
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
                    String refTable = fk.getReferencedTable();

                    // si c'est notre table actuelle : on passe
                    if(refTable.equals(t.getName())) continue;

                    boolean fkFound = false;
                    for(Table r : res) {
                        if(refTable.equals(r.getName())) {

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