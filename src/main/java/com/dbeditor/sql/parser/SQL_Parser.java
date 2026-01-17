package com.dbeditor.sql.parser;

import com.dbeditor.model.DatabaseSchema;

public interface SQL_Parser {

    /**
     * fonction pour récupérer une bdd
     * @param filePath chemin de stockage du fichier contenant la bdd
     * @return le schema de la bdd
     */
    public DatabaseSchema loadFromFile(String filePath);
    
}