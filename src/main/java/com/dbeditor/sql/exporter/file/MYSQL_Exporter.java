package com.dbeditor.sql.exporter.file;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.dbeditor.model.Column;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;

public class MYSQL_Exporter extends SQL_Exporter {
    
    @Override
    public void exportToSQL(DatabaseSchema schema, String filepath) throws IOException {
        StringBuilder sql = new StringBuilder();

        sql.append("DROP DATABASE IF EXISTS %s;\n".formatted(schema.getName())); 
        sql.append("CREATE DATABASE %s;\n".formatted(schema.getName())); 
        sql.append("USE %s;\n\n".formatted(schema.getName()));

        List<Table> orderedTables = super.sortTables(schema.getTables().values());
        for (Table table : orderedTables) {
            sql.append("CREATE TABLE %s (\n".formatted(table.getName()));
            
            for (int i = 0; i < table.getColumns().size(); i++) {
                Column col = table.getColumns().get(i);
                sql.append("\t%s %s".formatted(col.getName(), col.getType()));
                
                if (col.isPrimaryKey()) sql.append(" PRIMARY KEY");
                if (col.isNotNull()) sql.append(" NOT NULL");
                if (col.isUnique()) sql.append(" UNIQUE");
                
                if (i < table.getColumns().size() - 1 || !table.getForeignKeys().isEmpty()) {
                    sql.append(",");
                }
                sql.append("\n");
            }
            
            for (int i = 0; i < table.getForeignKeys().size(); i++) {
                ForeignKey fk = table.getForeignKeys().get(i);
                sql.append("\tCONSTRAINT %s FOREIGN KEY(%s) REFERENCES %s(%s)".formatted(fk.getFkName(), fk.getColumnName(), fk.getReferencedTable(), fk.getReferencedColumn()));
                
                if (i < table.getForeignKeys().size() - 1) {
                    sql.append(",");
                }
                sql.append("\n");
            }
            
            sql.append(");\n\n");
        }
        
        try (FileWriter writer = new FileWriter(filepath)) {
            writer.write(sql.toString());
        }
    }
}