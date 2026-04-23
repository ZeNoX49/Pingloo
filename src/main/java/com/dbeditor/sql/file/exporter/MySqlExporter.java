package com.dbeditor.sql.file.exporter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dbeditor.MainApp;

public class MySqlExporter extends SqlExporter {
    private static final Logger LOGGER = Logger.getLogger(MySqlExporter.class.getName());
        
    @Override
    public void exportToSQL(String filepath) {
        try (FileWriter writer = new FileWriter(filepath)) {
            writer.write(this.createSql());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public String createSql() {
        StringBuilder sql = new StringBuilder();

        sql.append("DROP DATABASE IF EXISTS %s;\n".formatted(MainApp.schema.name)); 
        sql.append("CREATE DATABASE %s;\n".formatted(MainApp.schema.name)); 
        sql.append("USE %s;\n\n".formatted(MainApp.schema.name));

        // List<Entity> orderedTables = DbManager.sortTables();
        // for (Entity table : orderedTables) {
        //     sql.append("CREATE TABLE %s (\n".formatted(table.name));
            
        //     for (int i = 0; i < table.getColumns().size(); i++) {
        //         Column col = table.getColumns().get(i);
        //         sql.append("\t%s %s".formatted(col.name, col.type.getRepr(DbType.MySql)));
                
        //         if (col.isPrimaryKey) sql.append(" PRIMARY KEY");
        //         if (col.isAutoIncrementing) sql.append(" AUTO_INCREMENT");
        //         if (col.isNotNull) sql.append(" NOT NULL");
        //         if (col.isUnique) sql.append(" UNIQUE");
                
        //         if (i < table.getColumns().size() - 1 || !table.getForeignKeys().isEmpty()) {
        //             sql.append(",");
        //         }
        //         sql.append("\n");
        //     }
            
        //     for (int i = 0; i < table.getForeignKeys().size(); i++) {
        //         ForeignKey fk = table.getForeignKeys().get(i);
        //         sql.append("\tCONSTRAINT %s FOREIGN KEY(%s) REFERENCES %s(%s)".formatted(fk.fkName, fk.columnName, fk.referencedTable, fk.referencedColumn));
                
        //         if (i < table.getForeignKeys().size() - 1) {
        //             sql.append(",");
        //         }
        //         sql.append("\n");
        //     }
            
        //     sql.append(");\n\n");
        // }

        return sql.toString();
    }
}