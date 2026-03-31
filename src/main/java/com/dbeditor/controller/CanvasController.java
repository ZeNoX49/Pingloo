package com.dbeditor.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.dbeditor.MainApp;
import com.dbeditor.controller.modifier.Visual;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.sql.file.exporter.MySqlExporter;
import com.dbeditor.sql.file.parser.MySqlParser;
import com.dbeditor.util.DbManager;
import com.dbeditor.util.FileManager;
import com.dbeditor.util.ThemeManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CanvasController implements Visual {
    private static final DbManager D_M = DbManager.getInstance();
    private static final ThemeManager T_M = ThemeManager.getInstance();
    private static final FileManager F_M = FileManager.getInstance();

    @FXML private StackPane spPane;
    @FXML private ToolBar toolBar;
    @FXML private MenuButton mbDatabase; // TODO
    // @FXML private Button btnRedo, btnUndo;
    @FXML private TextField tfDbName;
    @FXML private Region spacer1, spacer2;
    @FXML private Menu menuOpenDbMYSQL, menuSaveDbMYSQL;
    @FXML private MenuItem miLightTheme, miDarkTheme, miPersoTheme;
    @FXML private Label appNameLabel;

    private List<ViewController> views;

    @FXML
    private void initialize() throws IOException {
        this.views = new ArrayList<>();
        
        this.createBaseView();

        this.miLightTheme.setOnAction(e -> this.changeTheme(1));
        this.miDarkTheme.setOnAction(e -> this.changeTheme(2));
        this.miPersoTheme.setOnAction(e -> this.changeTheme(3));
        this.tfDbName.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused && MainApp.schema != null) {
                MainApp.schema.name = this.tfDbName.getText();
            }
        });

        HBox.setHgrow(this.spacer1, Priority.ALWAYS);
        HBox.setHgrow(this.spacer2, Priority.ALWAYS);

        this.createMenuItemMysql();

        this.updateStyle();
    }

    /**
     * Permettre de créer une vue de base<br>
     * dans ce cas le MCD
     * @throws IOException
     */
    private void createBaseView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view.fxml"));
        Pane mcdPane = loader.load();
        ViewController mcdController = loader.getController();

        // on fournit la fonction d'enregistrement au controller
        mcdController.setData(this.spPane, mcdPane, ViewType.MCD, (view) -> {
            this.views.add(view);
        });
        
        this.views.add(mcdController);

        mcdPane.setMinSize(0, 0);
        mcdPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        this.spPane.getChildren().add(mcdPane);
    }

    public void registerView(ViewController v) {
        this.views.add(v);
    }

    private void changeTheme(int idTheme) {
        T_M.changeTheme(idTheme);
        this.updateStyle();
    }

    @Override
    public void updateStyle() {
        this.spPane.setStyle("-fx-background-color: " + T_M.getTheme().getBackgroundColor() + ";");

        this.toolBar.setStyle(
            "-fx-background-color: " + T_M.getTheme().getToolbarColor() + 
            "; -fx-border-color: " + T_M.getTheme().getToolbarBorderColor() + 
            "; -fx-border-width: 0 0 1 0;"
        );

        this.appNameLabel.setStyle("-fx-text-fill: " + T_M.getTheme().getTextColor() + ";");

        for(ViewController v : this.views) {
            v.updateStyle();
        }
    }

    /**
     * Ouvre la fenêtre de modification du thème perso
     * @param event
     */
    @FXML
    void openPersoThemeParameter(ActionEvent event) {
        try {
	        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/parameter/theme.fxml"));
	        Scene scene = new Scene(loader.load());
			
			Stage modalStage = new Stage();
            modalStage.setTitle("Thème personelle - paramètre");
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.UTILITY);
            modalStage.setResizable(false);

            modalStage.setOnCloseRequest(e -> this.updateStyle());

	        modalStage.setScene(scene);
			modalStage.showAndWait();
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

        this.open(F_M.openDatabase(fileChooser, new MySqlParser()));
    }

    /**
     * Permet de charger un DatabaseSchema dans toutes les vues
     * @param dbS
     * @throws IOException
     */
    private void open(DatabaseSchema schema) throws IOException {
        if(schema != null) {
            MainApp.schema = schema;
            this.tfDbName.setText(schema.name);

            for(ViewController v : this.views) {
                v.open(schema);
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

        F_M.exportSQL(fileChooser, MainApp.schema, new MySqlExporter());
    }

    // void saveDbMYSQL(ActionEvent event) {
    //     System.out.println("saveDbMYSQL");
    // }

    /**
     * Créer les MenuItem pour chaque nom des bdd mysql
     * et leur associe l'action permettant de charger la bdd
     */
    private void createMenuItemMysql() {
        this.menuOpenDbMYSQL.getItems().clear();

        MenuItem mip = new MenuItem();
        ImageView img = new ImageView(new Image(MainApp.class.getResource("/img/parametre.png").toString(), 15, 15, true, true));
        mip.setGraphic(img);

        mip.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/parameter/mysql.fxml"));
                Scene scene = new Scene(loader.load());
                
                Stage modalStage = new Stage();
                modalStage.setTitle("MYSQL - paramètre");
                modalStage.initModality(Modality.APPLICATION_MODAL);
                modalStage.initStyle(StageStyle.UTILITY);
                modalStage.setResizable(false);

                modalStage.setOnCloseRequest(ev -> { this.createMenuItemMysql(); });

                modalStage.setScene(scene);
                modalStage.showAndWait();
            } catch (IOException ioe) {
                System.err.println("Erreur de chargement de la scène : mysql.fxml");
                ioe.printStackTrace();
            }
        });
        this.menuOpenDbMYSQL.getItems().add(mip);

        for(String tableName : D_M.getMysqlDbTables()) {
            MenuItem mi = new MenuItem(tableName);
            mi.setOnAction(e -> {
                try {
                    this.open(D_M.getMysqlDb().loadDb(tableName));
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            });
            this.menuOpenDbMYSQL.getItems().add(mi);
        }
    }

    /**
     * Affiche une alerte "warning"
     */
    public static void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte "confirmation"
     */
    public static boolean showConfirmationAlert(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return (result.isPresent() && result.get() == ButtonType.OK);
    }
    
}