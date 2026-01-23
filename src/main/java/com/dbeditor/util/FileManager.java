package com.dbeditor.util;

import java.io.File;
import java.io.IOException;

import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.sql.file.exporter.MYSQL_Exporter;
import com.dbeditor.sql.file.exporter.SQL_Exporter;
import com.dbeditor.sql.file.parser.MYSQL_FileParser;
import com.dbeditor.sql.file.parser.SQL_FileParser;

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

    private Stage stage;
    private File lastUsedDirectory;
 
    public DatabaseSchema openDatabase(FileChooser fileChooser, SQL_FileParser parser) {
        fileChooser.setInitialDirectory(this.lastUsedDirectory);

        File fileDir = fileChooser.showOpenDialog(this.stage);
        if (fileDir != null) {
            this.lastUsedDirectory = fileDir.getParentFile(); // Mémoriser le dossier
            
            parser = new MYSQL_FileParser();
            DatabaseSchema schema = parser.loadFromFile(fileDir.getAbsolutePath());
            if (schema != null && !schema.getTables().isEmpty()) {
                return schema;
            } else {
                System.err.println("Erreur lors du chargement de la base de données");
                return null;
            }
        }

        System.err.println("Le dossier n'existe pas ????");
        return null;
    }
    
    public void exportSQL(FileChooser fileChooser, DatabaseSchema schema, SQL_Exporter exporter) {
        fileChooser.setInitialDirectory(this.lastUsedDirectory);

        File file = fileChooser.showSaveDialog(this.stage);
        exporter = new MYSQL_Exporter();
        if (file != null) {
            try {
                exporter.exportToSQL(schema, file.getAbsolutePath());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export réussi");
                alert.setHeaderText(null);
                alert.setContentText("La base de données a été exportée avec succès !");
                alert.showAndWait();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Erreur lors de l'export");
                alert.showAndWait();
            }
        }
    }

    public void setStage(Stage s) { this.stage = s; }
    public void setLastUsedDirectory(String dir) { this.lastUsedDirectory = new File(dir); }
    public String getLastUserDirectory() { return this.lastUsedDirectory.toString(); }
    
}