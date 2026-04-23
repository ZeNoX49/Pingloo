package com.dbeditor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dbeditor.model.ConceptualSchema;
import com.dbeditor.util.FileManager;
import com.dbeditor.util.JsonManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {
	private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());
	
    public static ConceptualSchema schema;

    @Override
	public void start(Stage stage) throws IOException {
		JsonManager J_M = JsonManager.getInstance();
		FileManager F_M = FileManager.getInstance();
        try {
			J_M.load();

			MainApp.schema = new ConceptualSchema("");

	        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/canvas.fxml"));
	        Scene scene = new Scene(loader.load(), 1280, 720);

	        stage.setTitle("Pingloo - Visual Database Editor");
			stage.getIcons().add(new Image(MainApp.class.getResource("/img/logo-pingloo.png").toString()));
	        stage.setScene(scene);
	        stage.show();

			F_M.setStage(stage);

			stage.setOnCloseRequest(e -> J_M.save());
	    } catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Erreur de chargement de la scène : /fxml/canvas.fxml", e);
	    }
	} public static void main(String[] args) {
		deleteUselessLog();
		launch(args);
	}

	/**
	 * Supprime tous les logs inutiles de JavaFX
	 * - "javafx.fxml.FXMLLoader$ValueElement processValue"
	 * - "Loading FXML document with JavaFX API of version"
	 */
	private static void deleteUselessLog() {
		List<String> toDelete = List.of(
			"javafx.fxml.FXMLLoader$ValueElement processValue",
			"Loading FXML document with JavaFX API of version"
		);

		PrintStream originalErr = System.err;
		System.setErr(new PrintStream(new OutputStream() {
			private final StringBuilder sb = new StringBuilder();

			@Override
			public void write(int b) throws IOException {
				if (b == '\n') {
					String line = sb.toString();
					sb.setLength(0);

					// Vérifie si la ligne contient un des motifs à supprimer
					boolean shouldDelete = toDelete.stream().anyMatch(line::contains);
					if (!shouldDelete) {
						originalErr.println(line);
					}
				} else {
					sb.append((char)b);
				}
			}
		}, true));
	}
}