package com.dbeditor.util;

import java.io.File;
import java.io.IOException;

import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.sql.exporter.file.MYSQL_Exporter;
import com.dbeditor.sql.exporter.file.SQL_Exporter;
import com.dbeditor.sql.parser.file.MYSQL_Parser;
import com.dbeditor.sql.parser.file.SQL_Parser;

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
 
    public DatabaseSchema openDatabase(SQL_Parser parser) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ouvrir une base de données");

        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Fichiers MYSQL", "*.sql"),
            new FileChooser.ExtensionFilter("Fichiers MSSQL", "*.sql")
        );

        // Définir le répertoire en mémoire
        fileChooser.setInitialDirectory(lastUsedDirectory);

        File fileDir = fileChooser.showOpenDialog(stage);
        if (fileDir != null) {
            lastUsedDirectory = fileDir.getParentFile(); // Mémoriser le dossier
            
            parser = new MYSQL_Parser();
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
        // FileChooser fileChooser = new FileChooser();
        // fileChooser.setTitle("Exporter en SQL");

        // fileChooser.getExtensionFilters().add(
        //     new FileChooser.ExtensionFilter("Fichiers SQL", "*.sql")
        // );
        // fileChooser.setInitialFileName("export.sql");
        
        // Définir le répertoire en mémoire
        fileChooser.setInitialDirectory(lastUsedDirectory);

        File file = fileChooser.showSaveDialog(stage);
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

    public void setStage(Stage s) { stage = s; }
    public void setLastUsedDirectory(String dir) { lastUsedDirectory = new File(dir); }
    public String getLastUserDirectory() { return lastUsedDirectory.toString(); }
    
}