package com.dbeditor.sql.file.exporter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.dbeditor.model.Column;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;
import com.dbeditor.sql.DbType;
import com.dbeditor.util.DbManager;

public class MySqlExporter extends SqlExporter {
        
    @Override
    public void exportToSQL(DatabaseSchema schema, String filepath) throws IOException {
        try (FileWriter writer = new FileWriter(filepath)) {
            writer.write(this.createSql(schema));
        }
    }

    @Override
    public String createSql(DatabaseSchema schema) throws IOException {
        StringBuilder sql = new StringBuilder();

        sql.append("DROP DATABASE IF EXISTS %s;\n".formatted(schema.name)); 
        sql.append("CREATE DATABASE %s;\n".formatted(schema.name)); 
        sql.append("USE %s;\n\n".formatted(schema.name));

        List<Table> orderedTables = DbManager.sortTables(schema.getTables());
        for (Table table : orderedTables) {
            sql.append("CREATE TABLE %s (\n".formatted(table.name));
            
            for (int i = 0; i < table.getColumns().size(); i++) {
                Column col = table.getColumns().get(i);
                sql.append("\t%s %s".formatted(col.name, col.type.getRepr(DbType.MySql)));
                
                if (col.isPrimaryKey) sql.append(" PRIMARY KEY");
                if (col.isAutoIncrementing) sql.append(" AUTO_INCREMENT");
                if (col.isNotNull) sql.append(" NOT NULL");
                if (col.isUnique) sql.append(" UNIQUE");
                
                if (i < table.getColumns().size() - 1 || !table.getForeignKeys().isEmpty()) {
                    sql.append(",");
                }
                sql.append("\n");
            }
            
            for (int i = 0; i < table.getForeignKeys().size(); i++) {
                ForeignKey fk = table.getForeignKeys().get(i);
                sql.append("\tCONSTRAINT %s FOREIGN KEY(%s) REFERENCES %s(%s)".formatted(fk.fkName, fk.columnName, fk.referencedTable, fk.referencedColumn));
                
                if (i < table.getForeignKeys().size() - 1) {
                    sql.append(",");
                }
                sql.append("\n");
            }
            
            sql.append(");\n\n");
        }

        return sql.toString();
    }
}