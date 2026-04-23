package com.dbeditor.util;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dbeditor.controller.CanvasController;
import com.dbeditor.sql.file.exporter.SqlExporter;
import com.dbeditor.sql.file.parser.SqlParser;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FileManager {
    private static FileManager instance;
    public static FileManager getInstance() {
        if(instance == null) {
            instance = new FileManager();
        }
        return instance;
    }

    private FileManager() {}

    /* ================================================== */

    private static final Logger LOGGER = Logger.getLogger(FileManager.class.getName());

    private Stage stage;
    private File lastUsedDirectory;
 
    public void openDatabase(FileChooser fileChooser, SqlParser parser) {
        fileChooser.setInitialDirectory(this.lastUsedDirectory);

        File fileDir = fileChooser.showOpenDialog(this.stage);
        if (fileDir == null) {
            LOGGER.log(Level.SEVERE, "Le dossier n'existe pas ????");
            return;
        }

        this.lastUsedDirectory = fileDir.getParentFile();
        parser.loadFromFile(fileDir.getAbsolutePath());
    }
    
    public void exportSQL(FileChooser fileChooser, SqlExporter exporter) {
        fileChooser.setInitialDirectory(this.lastUsedDirectory);

        File file = fileChooser.showSaveDialog(this.stage);
        if (file == null) {
            LOGGER.log(Level.SEVERE, "Le fichier n'existe pas ????");
            return;
        }

        exporter.exportToSQL(file.getAbsolutePath());
        CanvasController.showWarningAlert("Export réussi", "La base de données a été exportée avec succès !");
    }

    public void setStage(Stage s) { this.stage = s; }
    public void setLastUsedDirectory(String dir) { this.lastUsedDirectory = new File(dir); }
    public String getLastUserDirectory() { return this.lastUsedDirectory.toString(); }
    
}