package com.dbeditor.controller.view;

import java.io.IOException;
import java.util.function.Consumer;

import com.dbeditor.MainApp;
import com.dbeditor.model.DatabaseSchema;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.util.Pair;

public abstract class View {

    public enum ViewType {
        MCD, MLD, DF, DD, SDF, VALUE;

        @Override
        public String toString() {
            if (this.equals(VALUE)) {
                return "Value";
            } return super.toString();
        }
    }

    /**
     * Donne le ViewType de la vue actuelle
     */
    public abstract ViewType getViewType();

    private Pane parent;
    private Pane viewPane;
    private Consumer<Pair<View, Pane>> registrar; // callback pour enregistrer la vue dans CanvasController
    private Popup popup;
    
    /**
     * Permet de charger les données à la création de la vue
     * @param parent le Canvas Controller et le conteneur parent
     * @param viewPane la Node root chargée depuis le FXML
     * @param registrar fonction fournie par CanvasController pour enregistrer la paire (controller, pane)
     */
    public void setData(Pane parent, Pane viewPane, Consumer<Pair<View, Pane>> registrar) {
        this.parent = parent;
        this.viewPane = viewPane;
        this.registrar = registrar;
    }

    /**
     * Permet de mettre à jour le style au lancement de l'app
     * ou lors d'un changement de style
     */
    public abstract void updateStyle();

    /**
     * Permet de charger une bdd
     * @param dbS -> le DatabaseSchema de la bdd
     */
    public abstract void open(DatabaseSchema dbS) throws IOException;

    /**
     * Permet de mettre à jour les vues lors d'un changement dans une vue
     */
    public abstract void onSync();
    
    public void createSplit(Node backgroundNode) {
        // écoute uniquement les clics droits sur le backgroundNode
        backgroundNode.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            // n'ouvrir que si clic droit ET que le target est bien le backgroundNode
            // (donc pas quand on clique sur une table/child)
            if (e.getButton() != MouseButton.SECONDARY) return;

            // On vérifie que le clic a touché directement le backgroundNode (pas un enfant)
            if (e.getTarget() != backgroundNode) return;

            // si déjà affiché -> masquer
            if (this.popup != null && this.popup.isShowing()) {
                this.popup.hide();
                e.consume();
                return;
            }

            // créer le popup
            this.popup = new Popup();
            this.popup.setAutoHide(true);
            this.popup.setAutoFix(true);

            VBox content = new VBox(8);
            content.setStyle(
                "-fx-background-color: #333333; " +
                "-fx-padding: 10; " +
                "-fx-background-radius: 6; " +
                "-fx-border-radius: 6; " +
                "-fx-border-color: #444444;"
            );

            content.getChildren().addAll(
                this.createSplitButton("verticale", Orientation.HORIZONTAL),
                this.createSplitButton("horizontal", Orientation.VERTICAL)
            );
            this.popup.getContent().add(content);

            // Positionner le popup à l'endroit du clic (coordonnées écran direct)
            this.popup.show(backgroundNode.getScene().getWindow(), e.getScreenX(), e.getScreenY());
            e.consume();
        });
    }

    private Button createSplitButton(String separationAxe, Orientation orientation) {
        Button splitBtn = new Button("séparation " + separationAxe);
        splitBtn.setFont(new Font(13));
        splitBtn.setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
        splitBtn.setOnAction(esh -> {
            this.popup.hide();
            this.doSplit(orientation);
        });
        return splitBtn;
    }

    /**
     *  méthode utilitaire à appeler depuis le bouton splitVertical.setOnAction(...)
     */
    private void doSplit(Orientation orientation) {
        try {
            Parent parentNode = this.viewPane.getParent();

            String viewTypeName = this.getViewType().toString().toLowerCase();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view/" + viewTypeName + ".fxml"));
            Pane newPane = loader.load();
            View newController = loader.getController();

            // Fournir setData à la nouvelle vue
            if (this.registrar != null) {
                newController.setData((Pane) parentNode, newPane, this.registrar);
            } else {
                newController.setData((Pane) parentNode, newPane, null);
            }

            // Politique de taille
            newPane.setMinSize(0, 0);
            newPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            this.viewPane.setMinSize(0, 0);
            this.viewPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // Enregistrer la nouvelle vue dans CanvasController
            if (this.registrar != null) {
                this.registrar.accept(new Pair<>(newController, newPane));
            }

            if (MainApp.getSchema() != null) {
                newController.open(MainApp.getSchema());
            }

            // ------------------------------
            // CASE A : parent est un Pane simple -> créer un SplitPane
            // ------------------------------
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

                // IMPORTANT : initialiser les dividers après que le SplitPane soit dans le scene graph
                Platform.runLater(() -> this.setupSplitMerge(sp));

                return;
            }

            // ------------------------------
            // CASE B : parent est déjà un SplitPane -> ajouter à côté
            // ------------------------------
            if (parentNode instanceof SplitPane spParent) {
                int i = spParent.getItems().indexOf(this.viewPane);
                if (i >= 0) {
                    spParent.getItems().add(i + 1, newPane);
                } else {
                    spParent.getItems().add(newPane);
                }

                // initialiser les dividers du SplitPane parent existant
                Platform.runLater(() -> this.setupSplitMerge(spParent));

                return;
            }

            // ------------------------------
            // fallback : ajouter au parent stocké
            // ------------------------------
            if (this.parent != null) {
                this.parent.getChildren().add(newPane);
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void setupSplitMerge(SplitPane sp) {
        sp.applyCss(); // important pour s'assurer que les dividers existent
        sp.layout();   // calculer la position des dividers

        for (Node divider : sp.lookupAll(".split-pane-divider")) {
            divider.setOnMouseClicked(e -> {
                if (e.getButton() != MouseButton.SECONDARY) return;

                if (this.popup != null && this.popup.isShowing()) {
                    this.popup.hide();
                }

                this.popup = new Popup();
                this.popup.setAutoHide(true);
                this.popup.setAutoFix(true);

                VBox content = new VBox(8);
                content.setStyle(
                    "-fx-background-color: #333333; " +
                    "-fx-padding: 10; " +
                    "-fx-background-radius: 6; " +
                    "-fx-border-radius: 6; " +
                    "-fx-border-color: #444444;"
                );

                Button mergeFirst = new Button(); // garder la première partie
                Button mergeSecond = new Button(); // garder la deuxième partie

                if (sp.getOrientation() == Orientation.HORIZONTAL) {
                    mergeFirst.setText("Garder la partie gauche");
                    mergeSecond.setText("Garder la partie droite");
                } else {
                    mergeFirst.setText("Garder la partie haute");
                    mergeSecond.setText("Garder la partie basse");
                }

                mergeFirst.setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
                mergeSecond.setStyle("-fx-text-fill: white; -fx-background-color: transparent;");

                mergeFirst.setOnAction(ev -> {
                    this.popup.hide();
                    mergeSplitPane(sp, true); // garder premier enfant
                });

                mergeSecond.setOnAction(ev -> {
                    this.popup.hide();
                    mergeSplitPane(sp, false); // garder deuxième enfant
                });

                content.getChildren().addAll(mergeFirst, mergeSecond);
                this.popup.getContent().add(content);

                this.popup.show(divider.getScene().getWindow(), e.getScreenX(), e.getScreenY());
                e.consume();
            });
        }
    }

    private void mergeSplitPane(SplitPane sp, boolean keepFirst) {
        if (sp.getItems().size() < 2) return;

        Pane p = (Pane) sp.getParent();
        int index = p.getChildren().indexOf(sp);
        if (index < 0) return;

        Node keep = keepFirst ? sp.getItems().get(0) : sp.getItems().get(1);
        sp.getItems().clear();

        p.getChildren().remove(index);
        p.getChildren().add(index, keep);

        if (keep instanceof Region r) {
            r.setMinSize(0, 0);
            r.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }
    }

    /**
     * Met a jour la combobox pour le changement de vue
     * @param cb -> ComboBox<String> de la toolbar
     * @param value -> Type de la vue actuelle
     */
    public void setupCombobowView(ComboBox<String> cb, ViewType value) {
        cb.getItems().clear();
        cb.getItems().addAll(
            ViewType.MCD.toString(),
            ViewType.MLD.toString()
        );

        // affichage initial
        cb.setValue(value.toString());

        cb.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.equals(oldValue)) return;

            cb.setValue(newValue.toString());

            System.out.println("Transformation en un " + newValue);
            String fxmlPath = "/fxml/view/" + this.getViewType().toString().toLowerCase() + ".fxml";

            try {

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


                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Pane newPane = loader.load();
                View newController = loader.getController();

                // fournir setData au nouveau controller (même parent, même registrar)
                newController.setData(this.parent, newPane, this.registrar);

                newPane.setMinSize(0, 0);
                newPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                
                // remplacer la vue
                this.parent.getChildren().remove(this.viewPane);
                this.parent.getChildren().add(newPane);

                // initialiser la nouvelle vue
                newController.open(MainApp.getSchema());

                // remplacer la référence locale viewPane pour ce controller
                this.viewPane = newPane;

                System.out.println("Transformation finie");

            } catch (IOException ex) {
                System.err.println("Erreur lors du chargement du FXML pour " + newValue);
                ex.printStackTrace();
            }
        });
    }
}