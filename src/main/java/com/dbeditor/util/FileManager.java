package com.dbeditor.util;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.sql.file.exporter.SqlExporter;
import com.dbeditor.sql.file.parser.SqlParser;

import javafx.scene.control.Alert;
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
 
    public DatabaseSchema openDatabase(FileChooser fileChooser, SqlParser parser) {
        fileChooser.setInitialDirectory(this.lastUsedDirectory);

        File fileDir = fileChooser.showOpenDialog(this.stage);
        if (fileDir != null) {
            this.lastUsedDirectory = fileDir.getParentFile(); // Mémoriser le dossier
            
            DatabaseSchema schema = parser.loadFromFile(fileDir.getAbsolutePath());
            if (schema != null && !schema.getTables().isEmpty()) {
                return schema;
            } else {
                LOGGER.log(Level.SEVERE, "Erreur lors du chargement de la base de données");
                return null;
            }
        }

        LOGGER.log(Level.SEVERE, "Le dossier n'existe pas ????");
        return null;
    }
    
    public void exportSQL(FileChooser fileChooser, DatabaseSchema schema, SqlExporter exporter) {
        fileChooser.setInitialDirectory(this.lastUsedDirectory);

        File file = fileChooser.showSaveDialog(this.stage);
        if (file != null) {
            exporter.exportToSQL(schema, file.getAbsolutePath());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export réussi");
            alert.setHeaderText(null);
            alert.setContentText("La base de données a été exportée avec succès !");
            alert.showAndWait();
        }
    }

    public void setStage(Stage s) { this.stage = s; }
    public void setLastUsedDirectory(String dir) { this.lastUsedDirectory = new File(dir); }
    public String getLastUserDirectory() { return this.lastUsedDirectory.toString(); }
    
}