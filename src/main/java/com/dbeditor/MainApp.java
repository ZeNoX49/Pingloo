package com.dbeditor;

import java.io.IOException;

import com.dbeditor.util.FileManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    
    @Override
	public void start(Stage stage) throws IOException {
        try {
	        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/canvas.fxml"));
	        Scene scene = new Scene(loader.load());

	        stage.setTitle("Pingloo - Visual Database Editor");
	        stage.setScene(scene);
	        stage.show();

			FileManager.setStage(stage);
	    } catch (IOException e) {
			e.printStackTrace();
			throw new Error("Erreur de chargement de la scène : /fxml/canvas.fxml");
	    }
	} public static void main(String[] args) { launch(args); }

}