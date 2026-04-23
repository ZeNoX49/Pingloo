package com.dbeditor.sql.file.parser;

public abstract class SqlParser {

    /**
     * fonction pour récupérer une bdd
     * @param filePath chemin de stockage du fichier contenant la bdd
     * @return le schema de la bdd
     */
    public abstract void loadFromFile(String filePath);
    
}