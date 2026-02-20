package com.dbeditor.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.dbeditor.MainApp;
import com.dbeditor.controller.view.View;
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
import javafx.scene.control.Menu;
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
import javafx.util.Pair;

public class CanvasController {
    private static final DbManager D_M = DbManager.getInstance();
    private static final ThemeManager T_M = ThemeManager.getInstance();
    private static final FileManager F_M = FileManager.getInstance();

    @FXML private StackPane spPane;
    @FXML private ToolBar toolBar;
    @FXML private Button btnRedo, btnUndo;
    @FXML private TextField tfDbName;
    @FXML private Region spacer1, spacer2;
    @FXML private Menu menuOpenDbMYSQL, menuSaveDbMYSQL;
    @FXML private MenuItem miLT, miDT, miPT;
    
    private List<Pair<View, Pane>> viewsPane;

    @FXML
    private void initialize() throws IOException {
        this.viewsPane = new ArrayList<>();
        
        this.createBaseView();

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

        this.createMenuItemMysql();

        this.updateStyle();
    }

    /**
     * Permettre de créer une vue de base<br>
     * dans ce cas le MCD
     * @throws IOException
     */
    private void createBaseView() throws IOException {
        // FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view/mcd.fxml"));
        // Pane mcdPane = loader.load();
        // View mcdController = loader.getController();

        // // on fournit la fonction d'enregistrement au controller
        // mcdController.setData(this.spPane, mcdPane, (pair) -> {
        //     // registrar : ajoute la paire dans la liste viewsPane
        //     this.viewsPane.add(pair);
        // });
        // // on enregistre explicitement la première vue aussi
        // this.viewsPane.add(new Pair<>(mcdController, mcdPane));

        // mcdPane.setMinSize(0, 0);
        // mcdPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // this.spPane.getChildren().add(mcdPane);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view/mld.fxml"));
        Pane mldPane = loader.load();
        View mldController = loader.getController();

        // on fournit la fonction d'enregistrement au controller
        mldController.setData(this.spPane, mldPane, (pair) -> {
            // registrar : ajoute la paire dans la liste viewsPane
            this.viewsPane.add(pair);
        });
        // on enregistre explicitement la première vue aussi
        this.viewsPane.add(new Pair<>(mldController, mldPane));

        mldPane.setMinSize(0, 0);
        mldPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        this.spPane.getChildren().add(mldPane);
    }

    public void registerView(View v, Pane pane) {
        this.viewsPane.add(new Pair<>(v, pane));
    }

    private void changeTheme(int idTheme) {
        T_M.changeTheme(idTheme);
        this.updateStyle();
    }

    /**
     * Permet de mettre à jour le style au lancement de l'app
     * ou lors d'un changement de style
     */
    private void updateStyle() {
        this.spPane.setStyle("-fx-background-color: " + T_M.getTheme().getBackgroundColor() + ";");

        this.toolBar.setStyle("-fx-background-color: " + T_M.getTheme().getToolbarColor() + 
                        "; -fx-border-color: " + T_M.getTheme().getToolbarBorderColor() + 
                        "; -fx-border-width: 0 0 1 0;");

        for(Pair<View, Pane> p : this.viewsPane) {
            View v = p.getKey();
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
    private void open(DatabaseSchema dbS) throws IOException {
        if(dbS != null) {
            MainApp.setSchema(dbS);
            this.tfDbName.setText(dbS.getName());

            for(Pair<View, Pane> p : this.viewsPane) {
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

        F_M.exportSQL(fileChooser, MainApp.getSchema(), new MySqlExporter());
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