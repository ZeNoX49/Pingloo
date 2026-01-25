package com.dbeditor.controller.view;

import java.io.IOException;
import java.util.function.Consumer;

import com.dbeditor.MainApp;
import com.dbeditor.model.DatabaseSchema;

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

            Button splitVertical = new Button("Split Vertical");
            splitVertical.setFont(new Font(13));
            splitVertical.setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
            splitVertical.setOnAction(esv -> {
                this.popup.hide();
                doSplit(Orientation.HORIZONTAL);
            });

            Button splitHorizontal = new Button("Split Horizontal");
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
            // load new view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view/mld.fxml"));
            Pane newPane = loader.load();
            View newController = loader.getController();

            // setData sur la nouvelle vue : fournir le meme registrar (si non null)
            if (this.registrar != null) {
                newController.setData((Pane) parentNode, newPane, this.registrar);
            } else {
                newController.setData((Pane) parentNode, newPane, null);
            }

            // Ensure sizing policy (so SplitPane can resize them)
            newPane.setMinSize(0, 0);
            newPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            this.viewPane.setMinSize(0, 0);
            this.viewPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // register the new view in CanvasController (if registrar present)
            if (this.registrar != null) {
                this.registrar.accept(new Pair<>(newController, newPane));
            }

            if(MainApp.getSchema() != null) {
                newController.open(MainApp.getSchema());
            }

            // CASE A : parent is a Pane (e.g. this.pane) -> replace the child by a SplitPane
            if (parentNode instanceof Pane p) {
                int idx = p.getChildren().indexOf(this.viewPane);
                if (idx < 0) {
                    // fallback: just add splitpane at end
                    SplitPane sp = new SplitPane();
                    sp.setOrientation(orientation);
                    sp.getItems().addAll(this.viewPane, newPane);
                    p.getChildren().add(sp);
                } else {
                    p.getChildren().remove(idx);
                    SplitPane sp = new SplitPane();
                    sp.setOrientation(orientation);
                    sp.getItems().addAll(this.viewPane, newPane);
                    // replace at same index
                    p.getChildren().add(idx, sp);
                }
                // done
                return;
            }

            // CASE B : parent is already a SplitPane -> insert next to current
            if (parentNode instanceof SplitPane spParent) {
                int i = spParent.getItems().indexOf(this.viewPane);
                if (i >= 0) {
                    spParent.getItems().add(i + 1, newPane);
                } else {
                    spParent.getItems().add(newPane);
                }
                return;
            }

            // fallback : append to original stored parent
            if (this.parent != null) {
                this.parent.getChildren().add(newPane);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void setupCombobowView(ComboBox<String> cb, String value) {
        cb.getItems().addAll(
            View.MLD,
            View.MCD
            // View.DF,
            // View.DD,
            // View.SDF,
            // View.VALUE
        );

        cb.setValue(value);

        // cb.valueProperty().addListener((obs, oldValue, newValue) -> {
        //     try {
        //         if(newValue.equals(View.MLD)) {
        //             FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view/mld.fxml"));

        //             this.parent.getChildren().remove(this.viewPane);

        //             this.viewPane = loader.load();

        //             this.parent.getChildren().add(this.viewPane);
        //         }
        //     } catch(IOException e) {
        //         e.printStackTrace();
        //     }
        // });
    }
}
