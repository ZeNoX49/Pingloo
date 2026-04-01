package com.dbeditor.sql.file.exporter;

import java.io.IOException;

import com.dbeditor.model.DatabaseSchema;

public class PostgreSqlExporter extends SqlExporter {
    
    @Override
    public void exportToSQL(DatabaseSchema schema, String filepath) throws IOException {
        // TODO
    }

    @Override
    public String createSql(DatabaseSchema schema) throws IOException {
        // TODO
        return "";
    }

}