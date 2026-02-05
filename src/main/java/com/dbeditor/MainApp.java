package com.dbeditor;

import java.io.IOException;

import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.util.FileManager;
import com.dbeditor.util.JsonManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private static DatabaseSchema schema;
	public static void setSchema(DatabaseSchema schema) { MainApp.schema = schema; }
	public static DatabaseSchema getSchema() { return MainApp.schema; }

    @Override
	public void start(Stage stage) throws IOException {
		JsonManager J_M = JsonManager.getInstance();
		FileManager F_M = FileManager.getInstance();
        try {
			J_M.load();

			MainApp.schema = new DatabaseSchema("");

	        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/canvas.fxml"));
	        Scene scene = new Scene(loader.load(), 1280, 720);

	        stage.setTitle("Pingloo - Visual Database Editor");
	        stage.setScene(scene);
	        stage.show();

			F_M.setStage(stage);

			stage.setOnCloseRequest(e -> J_M.save());
	    } catch (IOException e) {
			e.printStackTrace();
			throw new Error("Erreur de chargement de la scène : /fxml/canvas.fxml");
	    }
	} public static void main(String[] args) { launch(args); }
}