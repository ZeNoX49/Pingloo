package com.dbeditor.controller.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dbeditor.MainApp;
import com.dbeditor.controller.CanvasController;
import com.dbeditor.controller.TableController;
import com.dbeditor.controller.view.dialogs.AssociationEditorDialog;
import com.dbeditor.controller.view.dialogs.TableEditorDialog;
import com.dbeditor.controller.view.helpers.LassoSelector;
import com.dbeditor.controller.view.helpers.MultiDragManager;
import com.dbeditor.controller.view.helpers.SelectionModel;
import com.dbeditor.controller.view.helpers.ZoomPanHandler;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.Table;
import com.dbeditor.model.mcd.ConceptualSchema;
import com.dbeditor.util.ThemeManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

/**
 * McdController FINAL pour gérer un vrai MCD selon Merise
 * Version complète avec gestionnaire de connexions
 */
public class McdController extends View {
    private static final ThemeManager T_M = ThemeManager.getInstance();

    @FXML private BorderPane root;
    @FXML private ToolBar toolbar;
    @FXML private ComboBox<String> cb;
    @FXML private Button btnEntity, btnAssociation, btnConvert;
    @FXML private Label zlLabel;
    @FXML private Pane pane;
    @FXML private Group group;

    // Modèle de données MCD
    private ConceptualSchema conceptualSchema;
    
    // Nodes visuels
    private final Map<String, TableController> tableNodes = new HashMap<>();
    private final List<Line> connectionLines = new ArrayList<>();

    // Helpers
    private ZoomPanHandler zoomPan;
    private SelectionModel<TableController> selectionModel;
    private LassoSelector lasso;
    private MultiDragManager multiDrag;
    @FXML
    void initialize() throws IOException {
        super.setupCombobowView(this.cb, ViewType.MCD);

        // Initialiser le modèle de sélection -> visualizer appelle setSelected sur TableController
        this.selectionModel = new SelectionModel<>((tc, selected) -> tc.setSelected(selected));
        
        // Initialiser le zoom/pan
        this.zoomPan = new ZoomPanHandler(this.pane, this.group);
        this.zoomPan.setupEvents(this.zlLabel);
        this.zlLabel.setText("%.2f".formatted(this.zoomPan.getZoomLevel()));

        // Initialiser le multidrag
        this.multiDrag = new MultiDragManager(this.selectionModel);

        // Permet au node de ne pas sortir du pane (pour ne pas les voir au dessus de la toolbar)
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(this.pane.widthProperty());
        clip.heightProperty().bind(this.pane.heightProperty());
        this.pane.setClip(clip);

        // Initialiser le lasso
        List<TableController> tcList = new ArrayList<>();
        for (TableController tc : this.tableNodes.values()) {
            tcList.add(tc);
        }
        this.lasso = new LassoSelector(this.pane, this.group, tcList, this.selectionModel);
        this.lasso.setupEvents();

        this.updateStyle();

        // suppression
        Platform.runLater(() -> {
            Scene scene = root.getScene();
            if (scene != null) {
                scene.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.DELETE) {
                        try {
                            this.deleteSelectedTables();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void updateStyle() {
        this.pane.setStyle("-fx-background-color: " + T_M.getTheme().getBackgroundColor() + ";");
        this.toolbar.setStyle(
                "-fx-background-color: " + T_M.getTheme().getToolbarColor() + "; " + 
                "-fx-border-color: " + T_M.getTheme().getToolbarBorderColor() + "; " + 
                "-fx-border-width: 0 0 1 0;"
        );
        
        for (TableController tc : this.tableNodes.values()) {
            tc.updateStyle();
        }
    }

    @Override
    public void open(DatabaseSchema dbS) throws IOException {
        if (dbS == null) return;

        this.conceptualSchema = new ConceptualSchema(dbS);

        // supprime tous les nodes sauf selectionRect
        this.group.getChildren().removeIf(node -> node != this.lasso.getRect());

        this.tableNodes.clear();
        this.connectionLines.clear();

        this.createTableNodes(this.conceptualSchema);
        this.drawConnections();

        if (this.lasso != null) {
            this.lasso.getRect().toFront();
        }
    }

    @Override
    public void onChange() {}

    /**
     * Crée les nodes visuels pour les entités
     */
    private void createTableNodes(ConceptualSchema schema) throws IOException {
        int col = 0, row = 0;
        int cols = (int) Math.ceil(Math.sqrt(schema.getTables().size()));

        for (Table table : schema.getTables()) {
            double x = col * 250 + 50;
            double y = row * 200 + 50;
            
            this.createTableNode(table, x, y, false);

            col++;
            if (col >= cols) {
                col = 0;
                row++;
            }
        }

        // s'assure que le rectangle de séléction est devant
        if (this.lasso != null) {
            this.lasso.getRect().toFront();
        }
    }

    /**
     * Crée un node d'entité à une position donnée
     */
    private void createTableNode(Table table, double x, double y, boolean isAssociation) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/table.fxml"));
        AnchorPane tcPane = loader.load();
        TableController tcController = loader.getController();
        tcController.createTableNode(table);

        this.tableNodes.put(tcController.getTable().getName(), tcController);

        // Gérer la sélection
        tcController.setOnSelect((tc, e) -> this.handleSelection(tc, e));

        // Menu contextuel
        tcPane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                // si double clique gauche
                // on modifie la table
                if (e.getClickCount() == 2) {
                    if(isAssociation) {
                        this.editAssociation(tcController);
                    } else {
                        this.editTable(tcController);
                    }
                    e.consume();
                }
            }
        });

        // attache le node pour le multidrag
        this.multiDrag.attach(tcController);

        // position initiale
        tcPane.setLayoutX(x);
        tcPane.setLayoutY(y);

        this.group.getChildren().add(tcPane);
    }

    /**
     * Tracer tout les liens entre les tables
     * @throws IOException 
     */
    private void drawConnections() throws IOException {
        // Supprimer les anciennes lignes
        this.connectionLines.forEach(line -> this.group.getChildren().remove(line));
        this.connectionLines.clear();

        for (Pair<Table, Table> p : this.conceptualSchema.getLinks()) {
            // TODO: gros bourbier si le nom existe déja
            String associationName = p.getKey().getName() + "_" + p.getValue().getName();
            // TODO: calculer la position de l'association (p1.x + p2.x)/2 | (p1.y + p2.y)/2
            this.createTableNode(new Table(associationName), 0, 0, true);
            TableController ac = this.tableNodes.get(associationName); // pas optimal ca

            this.drawConnection(this.tableNodes.get(p.getKey().getName()), ac);
            this.drawConnection(this.tableNodes.get(p.getValue().getName()), ac);
        }
    }

    /**
     * Permet de tracer un lien entre une entité et une association
     */
    private void drawConnection(TableController from, TableController to) {
        double fromX = from.getRoot().getLayoutX() + from.getRoot().getWidth() / 2;
        double fromY = from.getRoot().getLayoutY() + from.getRoot().getHeight() / 2;
        double toX = to.getRoot().getLayoutX() + to.getRoot().getWidth() / 2;
        double toY = to.getRoot().getLayoutY() + to.getRoot().getHeight() / 2;

        Line line = new Line(fromX, fromY, toX, toY);
        line.setStroke(Color.web(T_M.getTheme().getSecondaryTextColor()));
        line.setStrokeWidth(2);
        line.getStrokeDashArray().addAll(5.0, 5.0);

        // ajoute la ligne derrière le node
        this.group.getChildren().add(0, line);
        this.connectionLines.add(line);
        
        // bind la ligne aux tables
        line.startXProperty().bind(
            from.getRoot().layoutXProperty()
                .add(from.getRoot().widthProperty().divide(2))
        );

        line.startYProperty().bind(
            from.getRoot().layoutYProperty()
                .add(from.getRoot().heightProperty().divide(2))
        );

        line.endXProperty().bind(
            to.getRoot().layoutXProperty()
                .add(to.getRoot().widthProperty().divide(2))
        );

        line.endYProperty().bind(
            to.getRoot().layoutYProperty()
                .add(to.getRoot().heightProperty().divide(2))
        );
    }

    /**
     * Gère la sélection d'une entité
     */
    private void handleSelection(TableController table, MouseEvent e) {
        if (e.isControlDown()) {
            this.selectionModel.toggle(table);
            return;
        }

        if (this.selectionModel.contains(table)) {
            // Enleve cette table du multi-drag
            table.getRoot().toFront();
            return;
        }
        
        this.selectionModel.clear();
        this.selectionModel.select(table);
    }

    // /* ============================================
    //    ACTIONS : Ajouter, Modifier, Supprimer
    //    ============================================ */

    /**
     * Ajoute une nouvelle entité
     */
    @FXML
    public void addEntity() throws IOException {
        TableEditorDialog dialog = new TableEditorDialog();
        dialog.showAndWait();

        if (dialog.isConfirmed()) {
            Table table = dialog.getResultTable();
            
            if (conceptualSchema.getTable(table.getName()) != null) {
                CanvasController.showWarningAlert("Erreur", "Une entité avec ce nom existe déjà.");
                return;
            }

            conceptualSchema.addTable(table);

            double x = (this.pane.getWidth() / 2) - 100;
            double y = (this.pane.getHeight() / 2) - 75;
            
            this.createTableNode(table, x, y, false);
        }
    }

    /**
     * Modifie une table existante
     * @param tableController le contrôleur de la table à modifier
     */
    private void editTable(TableController tableController) {
        DatabaseSchema schema = MainApp.getSchema();
        if (schema == null) return;

        Table oldTable = tableController.getTable();
        String oldName = oldTable.getName();

        // Ouvrir le dialogue avec les données existantes
        TableEditorDialog dialog = new TableEditorDialog(oldTable);
        dialog.showAndWait();

        if (dialog.isConfirmed()) {
            Table modifiedTable = dialog.getResultTable();
            String newName = modifiedTable.getName();

            // Si le nom a changé, vérifier qu'il n'existe pas déjà
            if (!oldName.equals(newName) && schema.getTable(newName) != null) {
                CanvasController.showWarningAlert("Erreur", "Une table avec ce nom existe déjà.");
                return;
            }

            try {
                // Supprimer l'ancienne table du schéma
                schema.removeTable(oldName);
                
                // Ajouter la table modifiée
                schema.addTable(modifiedTable);

                // Recréer tout le visuel (pour simplifier)
                this.open(schema);

            } catch (IOException e) {
                e.printStackTrace();
                CanvasController.showWarningAlert("Erreur", "Impossible de mettre à jour la table.");
            }
        }
    }

    /**
     * Ajoute une nouvelle association
     */
    @FXML
    public void addAssociation() {
        CanvasController.showWarningAlert("Erreur", "pas encore fonctionnelle");

        // if (this.conceptualSchema.getTables().isEmpty()) {
        //     CanvasController.showWarningAlert("Erreur", "Créez d'abord des entités avant de créer une association.");
        //     return;
        // }

        // List<Entity> entities = new ArrayList<>(conceptualSchema.getEntities().values());
        // AssociationEditorDialog dialog = new AssociationEditorDialog(entities);
        // dialog.showAndWait();

        // if (dialog.isConfirmed()) {
        //     Association newAssoc = dialog.getResultAssociation();
        //     conceptualSchema.addAssociation(newAssoc);
            
        //     try {
        //         openConceptualSchema(conceptualSchema);
        //     } catch (IOException e) {
        //         e.printStackTrace();
        //     }
        // }
    }

    /**
     * Modifie une association
     */
    private void editAssociation(TableController ac) {
        Table oldAssoc = ac.getTable();
        
        List<Table> entities = new ArrayList<>(conceptualSchema.getTables());
        AssociationEditorDialog dialog = new AssociationEditorDialog(entities, oldAssoc);
        dialog.showAndWait();

        // TODO: corriger ceci
        // if (dialog.isConfirmed()) {
        //     Table modifiedAssoc = dialog.getResultAssociation();
            
        //     conceptualSchema.removeAssociation(oldAssoc);
        //     conceptualSchema.addAssociation(modifiedAssoc);
            
        //     try {
        //         openConceptualSchema(conceptualSchema);
        //     } catch (IOException e) {
        //         e.printStackTrace();
        //     }
        // }
    }

    /**
     * Supprime les tables sélectionnées (touche Suppr)
     * @throws IOException 
     */
    public void deleteSelectedTables() throws IOException {
        List<TableController> selected = new ArrayList<>(this.selectionModel.getSelected());
        
        if (selected.isEmpty()) return;

        // Demander confirmation
        String message = selected.size() == 1 
            ? "Êtes-vous sûr de vouloir supprimer la table '" + selected.get(0).getTable().getName() + "' ?"
            : "Êtes-vous sûr de vouloir supprimer " + selected.size() + " tables ?";

        boolean res = CanvasController.showConfirmationAlert("Confirmation", "Supprimer les tables", message);
        if (res) {
            DatabaseSchema schema = MainApp.getSchema();

            for (TableController tc : selected) {
                // Supprimer du schéma
                schema.removeTable(tc.getTable().getName());

                // Supprimer le node visuel
                this.group.getChildren().remove(tc.getRoot());
                this.tableNodes.remove(tc);
            }

            // Vider la sélection
            this.selectionModel.clear();

            // Redessiner les connexions
            this.drawConnections();
        }
    }

    /**
     * Convertit le MCD en MLD
     */
    @FXML
    public void convertToMLD() {
        if (this.conceptualSchema == null || this.conceptualSchema.getTables().isEmpty()) {
            CanvasController.showWarningAlert("Information", "Le MCD est vide. Rien à convertir.");
            return;
        }

        try {
            DatabaseSchema mld = this.conceptualSchema.transformToDatabase();
            
            CanvasController.showWarningAlert("Conversion réussie (MCD → MLD)",
                    "Le MCD a été converti en MLD avec succès !\n\n" +
                    "Tables créées : " + mld.getTables().size() + "\n\n" +
                    "Vous pouvez maintenant basculer vers la vue MLD.");
            
            MainApp.setSchema(mld);
        } catch (Exception e) {
            e.printStackTrace();
            CanvasController.showWarningAlert("Erreur", "Erreur lors de la conversion : " + e.getMessage());
        }
    }
}