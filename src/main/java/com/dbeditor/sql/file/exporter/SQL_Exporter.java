package com.dbeditor.sql.file.exporter;

import java.io.IOException;

import com.dbeditor.model.DatabaseSchema;

public abstract class SQL_Exporter {
    
    /**
     * Permet de faire un export de la bdd si elle est valide
     * @param schema schema de la bdd
     * @param filepath chemin de sauvegarde
     */
    public abstract void exportToSQL(DatabaseSchema schema, String filepath) throws IOException;
    
}