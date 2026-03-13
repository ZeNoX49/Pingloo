package com.dbeditor.controller;

import java.io.IOException;

import com.dbeditor.MainApp;
import com.dbeditor.controller.view.McdController;
import com.dbeditor.controller.view.View;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.util.ThemeManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
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
                // case MLD : return new MldController();
                // case DF : return DF;
                // case DD : return DD;
                // case SDF : return SDF;
                // case Value : return VALUE;
            } return null;
        }
    }

    @FXML private BorderPane root;
    @FXML private ToolBar toolbar;
    @FXML private ComboBox<String> cb;
    @FXML private Button btnSync;
    @FXML private StackPane spPane;

    // private Pane parent;
    private View view;
    // private Consumer<Pair<ViewController, Pane>> registrar; // callback pour enregistrer la vue dans CanvasController
    private Popup popup;
    
    @FXML
    void initialize() {
        // vue de base
        ViewType viewType = ViewType.MCD;
        this.cb.setValue(viewType.toString());

        try {
            View newController = viewType.getController();
            newController.initialization(this.toolbar);
            Pane newPane = newController.getRoot();

            newPane.setMinSize(0, 0);
            newPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            
            this.spPane.getChildren().add(newPane);

            // initialiser la nouvelle vue
            newController.open(MainApp.getSchema());

            // remplacer la référence locale viewPane pour ce controller
            this.view = newController;
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du FXML pour " + viewType);
            e.printStackTrace();
        }

        this.setupCombobowView();
        // createSplit(this.spPane);
    }

    public ToolBar getToolBar() { return this.toolbar; }

    /**
     * gère la logique de cahngement de vue
     * @param cb -> ComboBox<String> de la toolbar
     * @param value -> Type de la vue actuelle
     */
    public void setupCombobowView() {
        this.cb.getItems().clear();
        this.cb.getItems().addAll(
            ViewType.MCD.toString(),
            ViewType.MLD.toString()
        );

        // affichage initial
        this.cb.setValue(this.view.getViewType().toString());

        this.cb.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.equals(oldValue)) return;

            this.createView(ViewType.toEnum(newValue));
        });
    }

    public void createView(ViewType viewType) {
        this.cb.setValue(viewType.toString());

        System.out.println("Transformation en un " + viewType);

        try {
            View newController = viewType.getController();
            newController.initialization(this.toolbar);
            Pane newPane = newController.getRoot();

            newPane.setMinSize(0, 0);
            newPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            
            // remplacer la vue
            this.spPane.getChildren().remove(this.view.getRoot());
            this.spPane.getChildren().add(newPane);

            // initialiser la nouvelle vue
            newController.open(MainApp.getSchema());

            // remplacer la référence locale viewPane pour ce controller
            this.view = newController;
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du FXML pour " + viewType);
            e.printStackTrace();
        }
    }

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

    // /**
    //  * Permet de charger les données à la création de la vue
    //  * @param parent le Canvas Controller et le conteneur parent
    //  * @param viewPane la Node root chargée depuis le FXML
    //  * @param registrar fonction fournie par CanvasController pour enregistrer la paire (controller, pane)
    //  */
    // public void setData(Pane parent, Pane viewPane, Consumer<Pair<ViewController, Pane>> registrar) {
    //     this.parent = parent;
    //     this.viewPane = viewPane;
    //     this.registrar = registrar;
    // }
    
    // public void createSplit(Node backgroundNode) {
    //     // écoute uniquement les clics droits sur le backgroundNode
    //     backgroundNode.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
    //         // n'ouvrir que si clic droit ET que le target est bien le backgroundNode
    //         // (donc pas quand on clique sur une table/child)
    //         if (e.getButton() != MouseButton.SECONDARY) return;

    //         // On vérifie que le clic a touché directement le backgroundNode (pas un enfant)
    //         if (e.getTarget() != backgroundNode) return;

    //         // si déjà affiché -> masquer
    //         if (this.popup != null && this.popup.isShowing()) {
    //             this.popup.hide();
    //             e.consume();
    //             return;
    //         }

    //         // créer le popup
    //         this.popup = new Popup();
    //         this.popup.setAutoHide(true);
    //         this.popup.setAutoFix(true);

    //         VBox content = new VBox(8);
    //         content.setStyle(
    //             "-fx-background-color: #333333; " +
    //             "-fx-padding: 10; " +
    //             "-fx-background-radius: 6; " +
    //             "-fx-border-radius: 6; " +
    //             "-fx-border-color: #444444;"
    //         );

    //         content.getChildren().addAll(
    //             this.createSplitButton("verticale", Orientation.HORIZONTAL),
    //             this.createSplitButton("horizontal", Orientation.VERTICAL)
    //         );
    //         this.popup.getContent().add(content);

    //         // Positionner le popup à l'endroit du clic (coordonnées écran direct)
    //         this.popup.show(backgroundNode.getScene().getWindow(), e.getScreenX(), e.getScreenY());
    //         e.consume();
    //     });
    // }

    // private Button createSplitButton(String separationAxe, Orientation orientation) {
    //     Button splitBtn = new Button("séparation " + separationAxe);
    //     splitBtn.setFont(new Font(13));
    //     splitBtn.setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
    //     splitBtn.setOnAction(esh -> {
    //         this.popup.hide();
    //         this.doSplit(orientation);
    //     });
    //     return splitBtn;
    // }

    // /**
    //  *  méthode utilitaire à appeler depuis le bouton splitVertical.setOnAction(...)
    //  */
    // private void doSplit(Orientation orientation) {
    //     try {
    //         Parent parentNode = this.viewPane.getParent();

    //         String viewTypeName = this.view.getViewType().toString().toLowerCase();
    //         FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view/" + viewTypeName + ".fxml"));
    //         Pane newPane = loader.load();
    //         View newController = loader.getController();

    //         // Fournir setData à la nouvelle vue
    //         if (this.registrar != null) {
    //             newController.setData((Pane) parentNode, newPane, this.registrar);
    //         } else {
    //             newController.setData((Pane) parentNode, newPane, null);
    //         }

    //         // Politique de taille
    //         newPane.setMinSize(0, 0);
    //         newPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    //         this.viewPane.setMinSize(0, 0);
    //         this.viewPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

    //         // Enregistrer la nouvelle vue dans CanvasController
    //         if (this.registrar != null) {
    //             this.registrar.accept(new Pair<>(newController, newPane));
    //         }

    //         if (MainApp.getSchema() != null) {
    //             newController.open(MainApp.getSchema());
    //         }

    //         // ------------------------------
    //         // CASE A : parent est un Pane simple -> créer un SplitPane
    //         // ------------------------------
    //         if (parentNode instanceof Pane p) {
    //             int idx = p.getChildren().indexOf(this.viewPane);

    //             SplitPane sp = new SplitPane();
    //             sp.setOrientation(orientation);
    //             sp.getItems().addAll(this.viewPane, newPane);

    //             if (idx < 0) {
    //                 p.getChildren().add(sp);
    //             } else {
    //                 p.getChildren().remove(idx);
    //                 p.getChildren().add(idx, sp);
    //             }

    //             // IMPORTANT : initialiser les dividers après que le SplitPane soit dans le scene graph
    //             Platform.runLater(() -> this.setupSplitMerge(sp));

    //             return;
    //         }

    //         // ------------------------------
    //         // CASE B : parent est déjà un SplitPane -> ajouter à côté
    //         // ------------------------------
    //         if (parentNode instanceof SplitPane spParent) {
    //             int i = spParent.getItems().indexOf(this.viewPane);
    //             if (i >= 0) {
    //                 spParent.getItems().add(i + 1, newPane);
    //             } else {
    //                 spParent.getItems().add(newPane);
    //             }

    //             // initialiser les dividers du SplitPane parent existant
    //             Platform.runLater(() -> this.setupSplitMerge(spParent));

    //             return;
    //         }

    //         // ------------------------------
    //         // fallback : ajouter au parent stocké
    //         // ------------------------------
    //         if (this.parent != null) {
    //             this.parent.getChildren().add(newPane);
    //         }

    //     } catch (IOException ioe) {
    //         ioe.printStackTrace();
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