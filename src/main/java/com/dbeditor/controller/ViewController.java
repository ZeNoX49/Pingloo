package com.dbeditor.controller;

import java.io.IOException;
import java.util.function.Consumer;

import com.dbeditor.MainApp;
import com.dbeditor.controller.view.McdController;
import com.dbeditor.controller.view.MldController;
import com.dbeditor.controller.view.View;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.util.ThemeManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

public class ViewController implements VisualModifier {
    private static final ThemeManager T_M = ThemeManager.getInstance();

    public enum ViewType {
        MCD, MLD, DF, DD, SDF, VALUE;

        @Override
        public String toString() {
            if (this.equals(VALUE)) {
                return "Value";
            } return super.toString();
        }

        public static ViewType toEnum(String value) {
            switch (value) {
                case "MCD" : return MCD;
                case "MLD" : return MLD;
                case "DF" : return DF;
                case "DD" : return DD;
                case "SDF" : return SDF;
                case "Value" : return VALUE;
            } return null;
        }

        public View getController() {
            switch (this) {
                case MCD : return new McdController();
                case MLD : return new MldController();
                // case DF : return DF;
                // case DD : return DD;
                // case SDF : return SDF;
                // case Value : return VALUE;
            } return null;
        }
    }

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
    public void setData(Pane parent, Pane viewPane, ViewType viewType, Consumer<ViewController> registrar) throws IOException {
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
        this.viewPane = newPane;

        // initialiser la nouvelle vue
        this.view.open(MainApp.getSchema());

        this.setupCombobowView();
        this.setupSplitHandlersOnViewRoot();
    }

    public ToolBar getToolBar() { return this.toolbar; }

    @Override
    public void updateStyle() {
        this.toolbar.setStyle(
            "-fx-background-color: " + T_M.getTheme().getToolbarColor() + "; " + 
            "-fx-border-color: " + T_M.getTheme().getToolbarBorderColor() + "; " + 
            "-fx-border-width: 0 0 1 0;"
        );
    }

    /**
     * Permet de charger une bdd
     * @param dbS -> le DatabaseSchema de la bdd
     */
    public void open(DatabaseSchema dbS) throws IOException {
        view.open(dbS);
    }

    /**
     * gère la logique de changement de vue
     */
    public void setupCombobowView() {
        this.cb.getItems().setAll(
            ViewType.MCD.toString(),
            ViewType.MLD.toString()
        );

        this.cb.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.equals(oldValue) || this.view == null) return;

            try {
                this.createView(ViewType.toEnum(newValue));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void createView(ViewType viewType) throws IOException {
        if (viewType == null) return;
        this.cb.setValue(viewType.toString());

        System.out.println("Transformation en un " + viewType);

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

            try {
                newController.open(MainApp.getSchema());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // re-attach view-root handlers for background clicks
            this.setupSplitHandlersOnViewRoot();
        });
    }

    private void setupSplitHandlersOnViewRoot() {
        if (this.view == null || this.view.getRoot() == null) return;

        Pane rootPane = this.view.getRoot();

        // Remove previous handler(s) if any to avoid duplicates
        rootPane.removeEventFilter(MouseEvent.MOUSE_CLICKED, this::onBackgroundClicked);
        rootPane.addEventFilter(MouseEvent.MOUSE_CLICKED, this::onBackgroundClicked);
    }

    private void onBackgroundClicked(MouseEvent e) {
        if (e.getButton() != MouseButton.SECONDARY) return;

        // Only open when clicking on the pane itself (empty space), not on children
        if (e.getTarget() != this.view.getRoot()) return;

        if (this.popup != null && this.popup.isShowing()) {
            this.popup.hide();
            e.consume();
            return;
        }

        this.popup = new Popup();
        this.popup.setAutoHide(true);
        this.popup.setAutoFix(true);

        VBox content = new VBox(8);
        content.setStyle("-fx-background-color: #333333; -fx-padding: 10; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #444444;");

        Button vBtn = new Button("séparation verticale");
        vBtn.setOnAction(a -> { this.popup.hide(); this.doSplit(Orientation.HORIZONTAL); });
        Button hBtn = new Button("séparation horizontale");
        hBtn.setOnAction(a -> { this.popup.hide(); this.doSplit(Orientation.VERTICAL); });

        content.getChildren().addAll(vBtn, hBtn);
        this.popup.getContent().add(content);

        if (this.view.getRoot().getScene() != null && this.view.getRoot().getScene().getWindow() != null) {
            this.popup.show(this.view.getRoot().getScene().getWindow(), e.getScreenX(), e.getScreenY());
        }

        e.consume();
    }
    
    private void doSplit(Orientation orientation)  {
        try {
            if (this.viewPane == null) return;

            Parent parentNode = this.viewPane.getParent();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view.fxml"));
            Pane newPane = loader.load();
            ViewController newController = loader.getController();

            // Provide the View instance to the new controller (create same view type)
            View newView = this.view.getViewType().getController();
            newController.setData((Pane) parentNode, newPane, newView.getViewType(), this.registrar);

            newPane.setMinSize(0, 0);
            newPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            this.viewPane.setMinSize(0, 0);
            this.viewPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            if (this.registrar != null) {
                this.registrar.accept(newController);
            }

            if (MainApp.getSchema() != null) {
                newController.open(MainApp.getSchema());
            }

            if (parentNode instanceof Pane p) {
                int idx = p.getChildren().indexOf(this.viewPane);

                SplitPane sp = new SplitPane();
                sp.setOrientation(orientation);
                sp.getItems().addAll(this.viewPane, newPane);

                if (idx < 0) {
                    p.getChildren().add(sp);
                } else {
                    p.getChildren().remove(idx);
                    p.getChildren().add(idx, sp);
                }

                Platform.runLater(() -> setupSplitMerge(sp));
                return;
            }

            if (parentNode instanceof SplitPane spParent) {
                int i = spParent.getItems().indexOf(this.viewPane);
                if (i >= 0) spParent.getItems().add(i + 1, newPane);
                else spParent.getItems().add(newPane);

                Platform.runLater(() -> setupSplitMerge(spParent));
                return;
            }

            if (this.parent != null) this.parent.getChildren().add(newPane);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void setupSplitMerge(SplitPane sp) {
        if (sp == null) return;

        Platform.runLater(() -> {
            sp.applyCss();
            sp.layout();

            // Add a single event filter on the splitpane to detect clicks near dividers
            sp.removeEventFilter(MouseEvent.MOUSE_PRESSED, this::onSplitPanePressed);
            sp.addEventFilter(MouseEvent.MOUSE_PRESSED, this::onSplitPanePressed);
        });
    }

    private void onSplitPanePressed(MouseEvent e) {
        if (e.getButton() != MouseButton.SECONDARY) return;

        Object src = e.getSource();
        if (!(src instanceof SplitPane sp)) return;

        Node closestDivider = null;
        double minDist = Double.MAX_VALUE;

        for (Node divider : sp.lookupAll(".split-pane-divider")) {
            double dx = e.getSceneX() - (divider.localToScene(divider.getBoundsInLocal()).getCenterX());
            double dy = e.getSceneY() - (divider.localToScene(divider.getBoundsInLocal()).getCenterY());
            double dist = Math.hypot(dx, dy);
            if (dist < minDist) {
                minDist = dist;
                closestDivider = divider;
            }
        }

        if (closestDivider != null && minDist < 20) { // tolerance in pixels
            showDividerPopup((SplitPane) e.getSource(), closestDivider, e.getScreenX(), e.getScreenY());
            e.consume();
        }
    }

    private void showDividerPopup(SplitPane sp, Node divider, double screenX, double screenY) {
        if (this.popup != null && this.popup.isShowing()) this.popup.hide();

        this.popup = new Popup();
        this.popup.setAutoHide(true);
        this.popup.setAutoFix(true);

        VBox content = new VBox(8);
        content.setStyle("-fx-background-color: #333333; -fx-padding: 10; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #444444;");

        Button mergeFirst = new Button(sp.getOrientation() == Orientation.HORIZONTAL ? "Garder la partie gauche" : "Garder la partie haute");
        Button mergeSecond = new Button(sp.getOrientation() == Orientation.HORIZONTAL ? "Garder la partie droite" : "Garder la partie basse");

        mergeFirst.setOnAction(ev -> { this.popup.hide(); mergeSplitPane(sp, true); });
        mergeSecond.setOnAction(ev -> { this.popup.hide(); mergeSplitPane(sp, false); });

        content.getChildren().addAll(mergeFirst, mergeSecond);
        this.popup.getContent().add(content);

        if (sp.getScene() != null && sp.getScene().getWindow() != null) this.popup.show(sp.getScene().getWindow(), screenX, screenY);
    }

    private void mergeSplitPane(SplitPane sp, boolean keepFirst) {
        if (sp == null) return;
        if (sp.getItems().size() < 2) return;

        Parent par = sp.getParent();
        if (!(par instanceof Pane p)) return;

        int index = p.getChildren().indexOf(sp);
        if (index < 0) return;

        Node keep = keepFirst ? sp.getItems().get(0) : sp.getItems().get(1);

        sp.getItems().clear();
        p.getChildren().remove(index);
        p.getChildren().add(index, keep);

        if (keep instanceof Pane p_) {
            p_.setMinSize(0, 0);
            p_.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            // update local reference if we merged the view that contained this controller
            this.viewPane = p_;
        }

        // If we have a registrar, re-register the remaining view controller later (CanvasController manages registry)
        Platform.runLater(() -> {
            if (this.registrar != null && keep.getUserData() instanceof ViewController) {
                this.registrar.accept((ViewController) keep.getUserData());
            }
        });
    }
}