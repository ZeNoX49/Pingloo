package com.dbeditor.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.dbeditor.MainApp;
import com.dbeditor.controller.modifier.Visual;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.sql.DbType;
import com.dbeditor.util.DbManager;
import com.dbeditor.util.FileManager;
import com.dbeditor.util.ThemeManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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

    @FXML private ToolBar toolBar;
    @FXML private Menu mOpenFile, mOpenDb;
    @FXML private Menu mSaveFile, mSaveDb;
    @FXML private MenuButton mbDatabase;
    @FXML private TextField tfDbName;
    @FXML private Region spacer1, spacer2;
    @FXML private MenuItem miLightTheme, miDarkTheme, miPersoTheme;
    @FXML private Label appNameLabel;
    @FXML private StackPane spPane;

    private List<ViewController> views;

    @FXML
    private void initialize() throws IOException {
        this.views = new ArrayList<>();

        for(DbType type : DbType.values()) {
            this.createMenuItemOpenFile(type);
            this.createMenuItemOpenDb(type);
            this.createMenuItemSaveFile(type);
            this.createMenuItemSaveDb(type);
            this.createMenuItemDatabse(type);
        }

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

        this.createBaseView();

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

    // utiliser lors des splits
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
            "-fx-background-color: " + T_M.getTheme().getToolbarColor() + ";" +
            "-fx-border-color: " + T_M.getTheme().getToolbarBorderColor() + ";" +
            "-fx-border-width: 0 0 1 0;"
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

    /**
     * Creér un Menu Item pour mOpenFile
     * @param type
     */
    private void createMenuItemOpenFile(DbType type) {
        MenuItem mi = new MenuItem(type.toString());
        mi.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Ouvrir une base de données");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers " + type.toString(), "*.sql")
            );

            try {
                this.open(F_M.openDatabase(fileChooser, D_M.getSqlParser(type)));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        this.mOpenFile.getItems().add(mi);
    }

    /**
     * Creér un Menu Item pour mOpenDb
     * @param type
     */
    private void createMenuItemOpenDb(DbType type) {
        Menu menu = new Menu(type.toString());
        this.mOpenDb.getItems().add(menu);

        this.createMenuItemParameterDb(menu);

        for(String dbName : D_M.getSqlTypeDatabases(type)) {
            MenuItem mi = new MenuItem(dbName);
            mi.setOnAction(e -> {
                try {
                    this.open(D_M.getSqlDb(type).loadDb(dbName));
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            });
            menu.getItems().add(mi);
        }
    }

    /**
     * Creér un Menu Item pour mSaveFile
     * @param type
     */
    private void createMenuItemSaveFile(DbType type) {
        MenuItem mi = new MenuItem(type.toString());
        mi.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Ouvrir une base de données");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers SQL", "*.sql")
            );
            fileChooser.setInitialFileName("export.sql");

            F_M.exportSQL(fileChooser, MainApp.schema, D_M.getSqlExporter(type));
        });
        this.mSaveFile.getItems().add(mi);
    }

    /**
     * Creér un Menu Item pour mSaveDb
     * @param type
     */
    private void createMenuItemSaveDb(DbType type) {
        Menu menu = new Menu(type.toString());
        this.mSaveDb.getItems().add(menu);

        this.createMenuItemParameterDb(menu);

        for(String dbName : D_M.getSqlTypeDatabases(type)) {
            MenuItem mi = new MenuItem(dbName);
            mi.setOnAction(e -> {
                try {
                    boolean good = D_M.getSqlDb(type).executeSqlScript(D_M.getSqlExporter(type).createSql(MainApp.schema));
                    if(good) {
                        CanvasController.showWarningAlert("Maj effectué", "La mise à jour de la bdd a été effectué");
                    } else {
                        CanvasController.showWarningAlert("Erreur", "Une erreur est survenu lors de la mise à jour de la bdd");
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            });
            menu.getItems().add(mi);
        }
    }

    /**
     * Creér un Menu Item pour mDatabase
     * @param type
     */
    private void createMenuItemDatabse(DbType type) {
        MenuItem mi = new MenuItem(type.toString());
        mi.setOnAction(e -> {
            MainApp.schema.type = type;
            for(ViewController v : this.views) {
                v.updateType(type);
            }
        });
        this.mbDatabase.getItems().add(mi);
    }

    /**
     * 
     */
    private void createMenuItemParameterDb(Menu menu) {
        menu.getItems().clear();

        ImageView img = new ImageView(new Image(MainApp.class.getResource("/img/parametre.png").toString(), 15, 15, true, true));
        MenuItem mip = new MenuItem("", img);

        mip.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/parameter/dbParam.fxml"));
                Scene scene = new Scene(loader.load());
                
                Stage modalStage = new Stage();
                modalStage.setTitle("db paramètre");
                modalStage.initModality(Modality.APPLICATION_MODAL);
                modalStage.initStyle(StageStyle.UTILITY);
                modalStage.setResizable(false);

                modalStage.setOnCloseRequest(ev -> this.createMenuItemParameterDb(menu));

                modalStage.setScene(scene);
                modalStage.showAndWait();
            } catch (IOException ioe) {
                System.err.println("Erreur de chargement de la scène : dbParam.fxml");
                ioe.printStackTrace();
            }
        });
        menu.getItems().add(mip);
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