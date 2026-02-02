package com.dbeditor.controller.view;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

import com.dbeditor.MainApp;
import com.dbeditor.model.DatabaseSchema;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
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
    public final static String MLD = "MLD";
    public final static String MCD = "MCD";
    public final static String DF = "DF";
    public final static String DD = "DD";
    public final static String SDF = "SDF";
    public final static String VALUE = "value";

    private Pane parent;
    private Pane viewPane;
    private Consumer<Pair<View, Pane>> registrar; // callback pour enregistrer la vue dans CanvasController
    private Popup popup;
    
    /**
     * @param parent : le conteneur parent (ex : zone centrale ou split pane)
     * @param viewPane : la Node root chargée depuis le FXML
     * @param registrar : fonction fournie par CanvasController pour enregistrer la paire (controller, pane)
     */
    public void setData(Pane parent, Pane viewPane, Consumer<Pair<View, Pane>> registrar) {
        this.parent = parent;
        this.viewPane = viewPane;
        this.registrar = registrar;
    }

    public abstract void updateStyle();
    public abstract void open(DatabaseSchema dbS) throws IOException;
    
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

            Button splitVertical = new Button("séparation verticale");
            splitVertical.setFont(new Font(13));
            splitVertical.setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
            splitVertical.setOnAction(esv -> {
                this.popup.hide();
                doSplit(Orientation.HORIZONTAL);
            });

            Button splitHorizontal = new Button("séparation horizontal");
            splitHorizontal.setFont(new Font(13));
            splitHorizontal.setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
            splitHorizontal.setOnAction(esh -> {
                this.popup.hide();
                doSplit(Orientation.VERTICAL);
            });

            content.getChildren().addAll(splitVertical, splitHorizontal);
            this.popup.getContent().add(content);

            // Positionner le popup à l'endroit du clic (coordonnées écran direct)
            this.popup.show(backgroundNode.getScene().getWindow(), e.getScreenX(), e.getScreenY());
            e.consume();
        });
    }

    /**
     *  méthode utilitaire à appeler depuis le bouton splitVertical.setOnAction(...)
     */
    private void doSplit(Orientation orientation) {
        try {
            Parent parentNode = this.viewPane.getParent();

            // Charger la nouvelle vue
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view/mld.fxml"));
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
                Platform.runLater(() -> setupSplitMerge(sp));

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
                Platform.runLater(() -> setupSplitMerge(spParent));

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

                Bounds screenBounds = divider.localToScreen(divider.getBoundsInLocal());
                this.popup.show(divider.getScene().getWindow(), screenBounds.getMinX(), screenBounds.getMinY());
                e.consume();
            });
        }
    }

    private void mergeSplitPane(SplitPane sp, boolean keepFirst) {
        if (sp.getItems().size() < 2) return;

        Pane parent = (Pane) sp.getParent();
        int index = parent.getChildren().indexOf(sp);
        if (index < 0) return;

        Node keep = keepFirst ? sp.getItems().get(0) : sp.getItems().get(1);
        sp.getItems().clear();

        parent.getChildren().remove(index);
        parent.getChildren().add(index, keep);

        if (keep instanceof Region r) {
            r.setMinSize(0, 0);
            r.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }
    }

    public void setupCombobowView(ComboBox<String> cb, String value) {
        // liste des vues disponibles
        cb.getItems().clear();
        cb.getItems().addAll(
            View.MLD,
            // View.MCD,
            View.DF
            // ajouter d'autres si besoin
        );

        // affichage initial
        if (value != null && cb.getItems().contains(value)) {
            cb.setValue(value);
        } else if (!cb.getItems().isEmpty()) {
            cb.setValue(cb.getItems().get(0));
        }

        cb.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.equals(oldValue)) return;

            // exécuter sur le thread UI
            Platform.runLater(() -> {
                String fxmlPath = switch (newValue) {
                    // case View.MCD -> "/fxml/view/mcd.fxml";
                    case View.DF  -> "/fxml/view/df.fxml";
                    case View.MLD -> "/fxml/view/mld.fxml";
                    default       -> "/fxml/view/mld.fxml";
                };

                // tentative robuste : essayer aussi quelques variantes de casse si le resource est null
                URL resource = getClass().getResource(fxmlPath);
                if (resource == null) {
                    // essais communs : tout en minuscule / tout en majuscule / MLD vs mld
                    String alt = fxmlPath.toLowerCase();
                    resource = getClass().getResource(alt);
                }
                if (resource == null) {
                    System.err.println("FXML introuvable pour '" + newValue + "'. Chemin essayé: " + fxmlPath);
                    return;
                }

                try {
                    FXMLLoader loader = new FXMLLoader(resource);
                    Pane newPane = loader.load();
                    View newController = loader.getController();

                    // fournir setData au nouveau controller (même parent, même registrar)
                    newController.setData(this.parent, newPane, this.registrar);

                    // sizing policy (si Region)
                    // if (newPane instanceof Region rNew) {
                    //     rNew.setMinSize(0, 0);
                    //     rNew.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    // }

                    // remplacer uniquement this.viewPane dans le parent (même position)
                    int idx = this.parent.getChildren().indexOf(this.viewPane);
                    if (idx >= 0) {
                        this.parent.getChildren().set(idx, newPane);
                    } else {
                        // fallback : ajout en fin
                        this.parent.getChildren().add(newPane);
                    }

                    // initialiser la nouvelle vue (open / style)
                    if (MainApp.getSchema() != null) {
                        try {
                            newController.open(MainApp.getSchema());
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    newController.updateStyle();

                    // remplacer la référence locale viewPane pour ce controller
                    this.viewPane = newPane;

                } catch (IOException ex) {
                    System.err.println("Erreur lors du chargement du FXML pour " + newValue);
                    ex.printStackTrace();
                }
            });
        });
    }
}
