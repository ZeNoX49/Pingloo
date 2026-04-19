package com.dbeditor.controller;

import java.util.function.Consumer;
import java.util.logging.Logger;

import com.dbeditor.controller.modifier.DbUpdate;
import com.dbeditor.controller.modifier.Visual;
import com.dbeditor.controller.view.View;
import com.dbeditor.util.ThemeManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;

public class ViewController implements Visual, DbUpdate {
    private static final Logger LOGGER = Logger.getLogger(ViewController.class.getName());

    private static final ThemeManager T_M = ThemeManager.getInstance();

    @FXML private BorderPane root;
    @FXML private ToolBar toolbar;
    @FXML private ComboBox<String> cb;
    @FXML private Button btnSync;
    @FXML private StackPane spPane;

    private Pane parent;
    private View view;
    private Pane viewPane;
    private Consumer<ViewController> registrar; // callback pour enregistrer la vue dans CanvasController
    private Popup popup;
    private ObservableList<Node> baseItemToolbar;
    
    @FXML
    void initialize() {
        this.baseItemToolbar = FXCollections.observableArrayList(toolbar.getItems());
    }

    /**
     * Permet de charger les données à la création de la vue
     * @param parent le Canvas Controller et le conteneur parent
     * @param viewPane
     * @param vieviewTypew
     * @param registrar fonction fournie par CanvasController pour enregistrer la paire (controller, pane)
     */
    public void setData(Pane parent, Pane viewPane, ViewType viewType, Consumer<ViewController> registrar) {
        this.parent = parent;
        this.viewPane = viewPane;
        this.view = viewType.getController();
        this.registrar = registrar;

        // vue de base
        this.cb.setValue(viewType.toString());

        this.view.initialization(this.toolbar);
        Pane newPane = this.view.getRoot();

        newPane.setMinSize(0, 0);
        newPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        this.spPane.getChildren().add(newPane);
        this.viewPane = newPane;   // TODO: a verif

        // initialiser la nouvelle vue
        this.view.open();

        this.setupCombobox();
        // this.setupSplit();
    }

    public ToolBar getToolBar() { return this.toolbar; }
    public Pane getRoot() { return this.root; }

    @Override
    public void updateStyle() {
        this.toolbar.setStyle(
            "-fx-background-color: " + T_M.getTheme().getToolbarColor() + "; " + 
            "-fx-border-color: " + T_M.getTheme().getToolbarBorderColor() + "; " + 
            "-fx-border-width: 0 0 1 0;"
        );
        this.view.updateStyle();
    }

    @Override
    public void open() {
        this.view.open();
    }

    @Override
    public void updateType() {
        // TODO
        this.view.updateType();
    }

    // TODO: sync

    /**
     * gère la logique de changement de vue
     */
    public void setupCombobox() {
        this.cb.getItems().setAll(
            ViewType.MCD.toString(),
            ViewType.MLD.toString(),
            ViewType.DF.toString(),
            ViewType.DD.toString(),
            ViewType.SDF.toString(),
            ViewType.VALUE.toString(),
            ViewType.SQL.toString()
        );

        this.cb.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.equals(oldValue) || this.view == null) return;

            this.createView(ViewType.toEnum(newValue));
        });
    }

    public void createView(ViewType viewType) {
        if (viewType == null) return;
        this.cb.setValue(viewType.toString());

        this.toolbar.getItems().setAll(this.baseItemToolbar);

        View newController = viewType.getController();
        newController.initialization(this.toolbar);
        Pane newPane = newController.getRoot();

        newPane.setMinSize(0, 0);
        newPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        Platform.runLater(() -> {
            this.spPane.getChildren().setAll(newPane);
            this.view = newController;
            this.viewPane = newPane;

            newController.open();

            // this.setupSplit();
        });
    }

    // private void setupSplit() {
    //     if (this.view == null || this.view.getRoot() == null) return;

    //     Pane rootPane = this.view.getRoot();

    //     // Remove previous handler(s) if any to avoid duplicates
    //     rootPane.removeEventFilter(MouseEvent.MOUSE_CLICKED, this::onBackgroundClicked);
    //     rootPane.addEventFilter(MouseEvent.MOUSE_CLICKED, this::onBackgroundClicked);
    // }

    // private void onBackgroundClicked(MouseEvent e) {
    //     if (e.getButton() != MouseButton.SECONDARY) return;

    //     // Only open when clicking on the pane itself (empty space), not on children
    //     if (e.getTarget() != this.view.getRoot()) return;

    //     if (this.popup != null && this.popup.isShowing()) {
    //         this.popup.hide();
    //         e.consume();
    //         return;
    //     }

    //     this.popup = new Popup();
    //     this.popup.setAutoHide(true);
    //     this.popup.setAutoFix(true);

    //     VBox content = new VBox(8);
    //     content.setStyle("-fx-background-color: #333333; -fx-padding: 10; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #444444;");

    //     Button vBtn = new Button("séparation verticale");
    //     vBtn.setOnAction(a -> { this.popup.hide(); this.doSplit(Orientation.HORIZONTAL); });
    //     Button hBtn = new Button("séparation horizontale");
    //     hBtn.setOnAction(a -> { this.popup.hide(); this.doSplit(Orientation.VERTICAL); });

    //     content.getChildren().addAll(vBtn, hBtn);
    //     this.popup.getContent().add(content);

    //     if (this.view.getRoot().getScene() != null && this.view.getRoot().getScene().getWindow() != null) {
    //         this.popup.show(this.view.getRoot().getScene().getWindow(), e.getScreenX(), e.getScreenY());
    //     }

    //     e.consume();
    // }
    
    // private void doSplit(Orientation orientation)  {
    //     try {
    //         if (this.viewPane == null) return;

    //         Parent parentNode = this.root.getParent();

    //         SplitPane sp = new SplitPane();
    //         sp.setOrientation(orientation);

    //         FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view.fxml"));
    //         Pane newPane = loader.load();
    //         ViewController newController = loader.getController();
            
    //         View newView = this.view.getViewType().getController();
    //         newController.setData((Pane) parentNode, newPane, newView.getViewType(), this.registrar);
    //         newController.updateStyle();

    //         newPane.setMinSize(0, 0);
    //         newPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    //         this.viewPane.setMinSize(0, 0);
    //         this.viewPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

    //         if (this.registrar != null) {
    //             this.registrar.accept(newController);
    //         }

    //         if (MainApp.getSchema() != null) {
    //             newController.open(MainApp.getSchema());
    //         }

    //         sp.getItems().addAll(this.root, newController.getRoot());

    //         if (parentNode instanceof Pane p) {
    //             int idx = p.getChildren().indexOf(this.root);
    //             p.getChildren().remove(idx);
    //             p.getChildren().add(idx, sp);

    //             Platform.runLater(() -> setupSplitMerge(sp));
    //             return;
    //         }

    //         if (parentNode instanceof SplitPane spParent) {
    //             int idx = spParent.getItems().indexOf(this.root);
    //             if (idx >= 0) spParent.getItems().add(idx + 1, newPane);
    //             else spParent.getItems().add(newPane);

    //             Platform.runLater(() -> setupSplitMerge(sp));
    //             return;
    //         }

    //         if (this.parent != null) this.parent.getChildren().add(newPane);

    //     } catch (IOException ioe) {
    //         LOGGER.log(Level.SEVERE, "", ioe);
    //     }
    // }

    // private void setupSplitMerge(SplitPane sp) {
    //     sp.applyCss(); // important pour s'assurer que les dividers existent
    //     sp.layout();   // calculer la position des dividers

    //     for (Node divider : sp.lookupAll(".split-pane-divider")) {
    //         divider.setOnMouseClicked(e -> {
    //             if (e.getButton() != MouseButton.SECONDARY) return;

    //             if (this.popup != null && this.popup.isShowing()) {
    //                 this.popup.hide();
    //             }

    //             this.popup = new Popup();
    //             this.popup.setAutoHide(true);
    //             this.popup.setAutoFix(true);

    //             VBox content = new VBox(8);
    //             content.setStyle(
    //                 "-fx-background-color: #333333; " +
    //                 "-fx-padding: 10; " +
    //                 "-fx-background-radius: 6; " +
    //                 "-fx-border-radius: 6; " +
    //                 "-fx-border-color: #444444;"
    //             );

    //             Button mergeFirst = new Button(); // garder la première partie
    //             Button mergeSecond = new Button(); // garder la deuxième partie

    //             if (sp.getOrientation() == Orientation.HORIZONTAL) {
    //                 mergeFirst.setText("Garder la partie gauche");
    //                 mergeSecond.setText("Garder la partie droite");
    //             } else {
    //                 mergeFirst.setText("Garder la partie haute");
    //                 mergeSecond.setText("Garder la partie basse");
    //             }

    //             mergeFirst.setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
    //             mergeSecond.setStyle("-fx-text-fill: white; -fx-background-color: transparent;");

    //             mergeFirst.setOnAction(ev -> {
    //                 this.popup.hide();
    //                 mergeSplitPane(sp, true); // garder premier enfant
    //             });

    //             mergeSecond.setOnAction(ev -> {
    //                 this.popup.hide();
    //                 mergeSplitPane(sp, false); // garder deuxième enfant
    //             });

    //             content.getChildren().addAll(mergeFirst, mergeSecond);
    //             this.popup.getContent().add(content);

    //             this.popup.show(divider.getScene().getWindow(), e.getScreenX(), e.getScreenY());
    //             e.consume();
    //         });
    //     }
    // }

    // private void mergeSplitPane(SplitPane sp, boolean keepFirst) {
    //     if (sp.getItems().size() < 2) return;

    //     Pane p = (Pane) sp.getParent();
    //     int index = p.getChildren().indexOf(sp);
    //     if (index < 0) return;

    //     Node keep = keepFirst ? sp.getItems().get(0) : sp.getItems().get(1);
    //     sp.getItems().clear();

    //     p.getChildren().remove(index);
    //     p.getChildren().add(index, keep);

    //     if (keep instanceof Region r) {
    //         r.setMinSize(0, 0);
    //         r.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    //     }
    // }
}