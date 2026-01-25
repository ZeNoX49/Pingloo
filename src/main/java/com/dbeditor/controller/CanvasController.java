package com.dbeditor.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.dbeditor.MainApp;
import com.dbeditor.controller.view.View;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.sql.file.exporter.MYSQL_Exporter;
import com.dbeditor.sql.file.parser.MYSQL_FileParser;
import com.dbeditor.util.DbManager;
import com.dbeditor.util.FileManager;
import com.dbeditor.util.ThemeManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;

public class CanvasController {
    private final DbManager D_M = DbManager.getInstance();
    private final ThemeManager T_M = ThemeManager.getInstance();
    private final FileManager F_M = FileManager.getInstance();

    @FXML private StackPane spPane;
    @FXML private ToolBar toolBar;
    @FXML private Button btnRedo, btnUndo;
    @FXML private TextField tfDbName;
    @FXML private Region spacer1, spacer2;
    @FXML private Menu menuOpenDbMYSQL, menuSaveDbMYSQL;
    @FXML private MenuItem miLT, miDT, miPT;
    
    private List<Pair<View, Pane>> vues_pane;

    @FXML
    private void initialize() throws IOException {
        this.vues_pane = new ArrayList<>();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view/mld.fxml"));
        Pane mldPane = loader.load();
        View mldController = loader.getController();
        // on fournit la fonction d'enregistrement au controller
        mldController.setData(this.spPane, mldPane, (pair) -> {
            // registrar : ajoute la paire dans la liste vues_pane
            this.vues_pane.add(pair);
        });
        // on enregistre explicitement la première vue aussi
        this.vues_pane.add(new Pair<>(mldController, mldPane));

        // IMPORTANT : ne lie pas prefWidth à pane.widthProperty() ici.
        // Le Pane parent ajouté dans un SplitPane ou dans this.pane gérera la taille.
        // Si tu veux absolument binder (si ton parent est un simple Pane), fais-le conditionnellement.
        mldPane.setMinSize(0, 0);
        mldPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        this.spPane.getChildren().add(mldPane);

        this.miLT.setOnAction(e -> this.changeTheme(1));
        this.miDT.setOnAction(e -> this.changeTheme(2));
        this.miPT.setOnAction(e -> this.changeTheme(3));
        this.tfDbName.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused && MainApp.getSchema() != null) {
                MainApp.getSchema().setName(this.tfDbName.getText());
            }
        });

        HBox.setHgrow(this.spacer1, Priority.ALWAYS);
        HBox.setHgrow(this.spacer2, Priority.ALWAYS);

        for(String tableName : D_M.getMysqlDbTables()) {
            MenuItem mi = new MenuItem(tableName);
            mi.setOnAction(e -> {
                try {
                    this.openDbMYSQL(tableName);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            });
            this.menuOpenDbMYSQL.getItems().add(mi);
        }

        this.updateStyle();
    }

    public void registerView(View v, Pane pane) {
        this.vues_pane.add(new Pair<>(v, pane));
    }

    private void changeTheme(int idTheme) {
        T_M.changeTheme(idTheme);
        this.updateStyle();
    }

    private void updateStyle() {
        this.spPane.setStyle("-fx-background-color: " + T_M.getTheme().getBackgroundColor() + ";");

        this.toolBar.setStyle("-fx-background-color: " + T_M.getTheme().getToolbarColor() + 
                        "; -fx-border-color: " + T_M.getTheme().getToolbarBorderColor() + 
                        "; -fx-border-width: 0 0 1 0;");

        for(Pair<View, Pane> p : this.vues_pane) {
            View v = p.getKey();
            v.updateStyle();
        }
    }

    @FXML
    void openPersoThemeParameter(ActionEvent event) {
        try {
	        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/theme.fxml"));
	        Scene scene = new Scene(loader.load());
			
			Stage modalStage = new Stage();
            modalStage.setTitle("Thème personelle - paramètre");
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.UTILITY);
            modalStage.setResizable(false);

	        modalStage.setScene(scene);
			modalStage.showAndWait();

            modalStage.setOnCloseRequest(e -> this.updateStyle());
	    } catch (IOException e) {
	        System.err.println("Erreur de chargement de la scène : theme.fxml");
	        e.printStackTrace();
	    }
    }

    @FXML
    void openFileMYSQL(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ouvrir une base de données");

        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers MYSQL", "*.sql")
        );

        this.open(this.F_M.openDatabase(fileChooser, new MYSQL_FileParser()));
    }

    void openDbMYSQL(String dbName) throws IOException {
        this.open(this.D_M.getMysqlDb().loadDb(dbName));
    }

    private void open(DatabaseSchema dbS) throws IOException {
        if(dbS != null) {
            MainApp.setSchema(dbS);
            this.tfDbName.setText(dbS.getName());

            for(Pair<View, Pane> p : this.vues_pane) {
                View v = p.getKey();
                v.open(dbS);
            }
        }
    }

    @FXML
    void saveFileMYSQL(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en SQL");

        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers SQL", "*.sql")
        );
        fileChooser.setInitialFileName("export.sql");

        this.F_M.exportSQL(fileChooser, MainApp.getSchema(), new MYSQL_Exporter());
    }

    // void saveDbMYSQL(ActionEvent event) {
    //     System.out.println("saveDbMYSQL");
    // }
}