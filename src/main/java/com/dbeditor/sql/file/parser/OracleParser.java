package com.dbeditor.sql.file.parser;

import com.dbeditor.model.DatabaseSchema;

public class OracleParser extends SqlParser {
    
    @Override
    public DatabaseSchema loadFromFile(String filePath) {
        // TODO
        return new DatabaseSchema("");
    }
    
}