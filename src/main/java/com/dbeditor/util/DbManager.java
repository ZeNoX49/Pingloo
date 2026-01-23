package com.dbeditor.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dbeditor.sql.db.MYSQL_Db;
import com.dbeditor.sql.db.SQL_Db;

public class DbManager {
    private static DbManager instance;
    public static DbManager getInstance() {
        if(instance == null) {
            instance = new DbManager();
        }
        return instance;
    }

    /* ================================================== */

    private Map<String, SQL_Db> db;
    private Map<String, List<String>> tables;
    // private Map<String, SQL_DbExporter> dbExporters;

    private DbManager() {
        this.db = new HashMap<>();
    }

    public void setMysqlDbData(Map<String, Object> data) {
        this.db.put("mysql", new MYSQL_Db(
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

    public MYSQL_Db getMysqlDb() { return (MYSQL_Db) this.db.get("mysql");  }
    public List<String> getMysqlDbTables() { return this.tables.get("mysql");  }

}