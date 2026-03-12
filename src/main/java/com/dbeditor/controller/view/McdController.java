package com.dbeditor.controller.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dbeditor.MainApp;
import com.dbeditor.controller.CanvasController;
import com.dbeditor.controller.TableController;
import com.dbeditor.controller.TableController.TableType;
import com.dbeditor.controller.view.dialogs.AssociationEditorDialog;
import com.dbeditor.controller.view.dialogs.TableEditorDialog;
import com.dbeditor.controller.view.helpers.LassoSelector;
import com.dbeditor.controller.view.helpers.MultiDragManager;
import com.dbeditor.controller.view.helpers.SelectionModel;
import com.dbeditor.controller.view.helpers.ZoomPanHandler;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.Table;
import com.dbeditor.model.mcd.CardinalityValue;
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
public class McdController extends ModelView {
    @Override
    public ViewType getViewType() {
        return ViewType.MCD;
    }
    
    private static final ThemeManager T_M = ThemeManager.getInstance();

    @FXML private BorderPane root;
    @FXML private ToolBar toolbar;
    @FXML private ComboBox<String> cb;
    @FXML private Button btnSync, btnEntity, btnAssociation, btnConvert;
    @FXML private Label zlLabel;
    @FXML private Pane pane;
    @FXML private Group group;

    // Modèle de données MCD
    private ConceptualSchema conceptualSchema;
    
    // Nodes visuels
    private final Map<String, TableController> tableNodes = new HashMap<>();
    private final List<Pair<Line, Label>> connectionLines = new ArrayList<>();
    private final List<TableController> tcList = new ArrayList<>();

    // Helpers
    private ZoomPanHandler zoomPan;
    private SelectionModel<TableController> selectionModel;
    private LassoSelector lasso;
    private MultiDragManager multiDrag;
    
    @FXML
    void initialize() throws IOException {
        this.conceptualSchema = new ConceptualSchema(MainApp.getSchema());

        super.setupCombobowView(this.cb, this.getViewType());
        super.createSplit(this.pane);

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

        // Initialiser le lasso avec la liste partagée (vide pour l'instant)
        this.lasso = new LassoSelector(this.pane, this.group, this.tcList, this.selectionModel);
        this.lasso.setupEvents();

        this.updateStyle();

        // suppression (touche DEL)
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

        for(Pair<Line, Label> p : this.connectionLines) {
            p.getValue().setStyle(
                "-fx-background-color: " + T_M.getTheme().getBackgroundColor() + "; " + 
                "-fx-text-fill: " + T_M.getTheme().getTextColor() + ";" +
                "-fx-font-size: 15;"
            );
        }
    }

    @Override
    public void open(DatabaseSchema dbS) throws IOException {
        if (dbS == null) return;

        // Recréer le ConceptualSchema à partir du DatabaseSchema donné
        this.conceptualSchema = new ConceptualSchema(dbS);

        // supprime tous les nodes sauf selectionRect
        this.group.getChildren().removeIf(node -> node != this.lasso.getRect());

        // vider les structures
        this.tableNodes.clear();
        this.connectionLines.clear();
        this.tcList.clear();

        // créer les nodes à partir du MCD
        this.createTableNodes(this.conceptualSchema);
        this.drawConnections();

        if (this.lasso != null) {
            this.lasso.getRect().toFront();
        }

        this.updateStyle();
    }

    @Override
    public void onSync() {}

    /**
     * Crée les nodes visuels pour les entités
     */
    private void createTableNodes(ConceptualSchema schema) throws IOException {
        int col = 0, row = 0;
        int cols = (int) Math.ceil(Math.sqrt(schema.getTables().size()));

        for (Table table : schema.getTables()) {
            double x = col * 250 + 50;
            double y = row * 200 + 50;
            
            this.createTableNode(table, x, y, TableType.Entite);

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
    private void createTableNode(Table table, double x, double y, TableType tabletype) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/table.fxml"));
        AnchorPane tcPane = loader.load();
        TableController tcController = loader.getController();
        tcController.createTableNode(table, tabletype);

        this.tableNodes.put(tcController.getTable().getName(), tcController);

        // Ajout dans la liste utilisée par le lasso (liste partagée)
        this.tcList.add(tcController);

        // Gérer la sélection
        tcController.setOnSelect((tc, e) -> this.handleSelection(tc, e));

        // Menu contextuel
        tcPane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                // si double clique gauche -> modifier la table ou l'association
                if (e.getClickCount() == 2) {
                    if(tabletype.equals(TableType.Entite)) {
                        this.editTable(tcController);
                    } else {
                        this.editAssociation(tcController);
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

        Map<String, List<Pair<Table, CardinalityValue>>> links = this.conceptualSchema.getLinks();
        for (String name : links.keySet()) {
            // TODO: modifier la position de base des associations
            this.createTableNode(this.conceptualSchema.getAssociationTable(name), 0, 0, TableType.Association);
            TableController ac = this.tableNodes.get(name);

            for(Pair<Table, CardinalityValue> p : links.get(name)) {
                this.drawConnection(this.tableNodes.get(p.getKey().getName()), ac, p.getValue());
            }
        }
    }

    /**
     * Permet de tracer un lien entre une entité et une association
     */
    private void drawConnection(TableController from, TableController to, CardinalityValue cardinality) {
        if (from == null || to == null) return;

        double fromX = from.getRoot().getLayoutX() + from.getRoot().getWidth() / 2;
        double fromY = from.getRoot().getLayoutY() + from.getRoot().getHeight() / 2;
        double toX = to.getRoot().getLayoutX() + to.getRoot().getWidth() / 2;
        double toY = to.getRoot().getLayoutY() + to.getRoot().getHeight() / 2;

        Line line = new Line(fromX, fromY, toX, toY);
        line.setStroke(Color.web(T_M.getTheme().getSecondaryTextColor()));
        line.setStrokeWidth(2);
        line.getStrokeDashArray().addAll(5.0, 5.0);
        
        // bind la ligne aux tables
        line.startXProperty().bind(from.getRoot().layoutXProperty().add(from.getRoot().widthProperty().divide(2)));
        line.startYProperty().bind(from.getRoot().layoutYProperty().add(from.getRoot().heightProperty().divide(2)));
        line.endXProperty().bind(to.getRoot().layoutXProperty().add(to.getRoot().widthProperty().divide(2)));
        line.endYProperty().bind(to.getRoot().layoutYProperty().add(to.getRoot().heightProperty().divide(2)));
    
        // Texte de la cardinalité
        Label cardinalityLabel = new Label(cardinality.toString());
        cardinalityLabel.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (e.getClickCount() == 2) {
                    // TODO: pouvoir modifier la cardinalité
                }
            }
        });

        // Bind le Label au centre de la ligne
        cardinalityLabel.layoutXProperty().bind(
            line.startXProperty().add(line.endXProperty().subtract(line.startXProperty()).divide(2))
                    .subtract(cardinalityLabel.widthProperty().divide(2)) // centrer horizontalement
        );
        cardinalityLabel.layoutYProperty().bind(
            line.startYProperty().add(line.endYProperty().subtract(line.startYProperty()).divide(2))
                    .subtract(cardinalityLabel.heightProperty().divide(2)) // centrer verticalement
        );

        // ajoute la ligne derrière le node
        this.group.getChildren().add(0, cardinalityLabel);
        this.group.getChildren().add(0, line);
        this.connectionLines.add(new Pair<>(line, cardinalityLabel));
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

            // TODO: Mettre à jour le modèle conceptuel
            // conceptualSchema.addTable(table);

            double x = (this.pane.getWidth() / 2) - 100;
            double y = (this.pane.getHeight() / 2) - 75;
            
            this.createTableNode(table, x, y, TableType.Entite);
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
    }

    /**
     * Modifie une association
     */
    private void editAssociation(TableController ac) {
        Table oldAssoc = ac.getTable();
        
        List<Table> entities = new ArrayList<>(conceptualSchema.getTables());
        AssociationEditorDialog dialog = new AssociationEditorDialog(entities, oldAssoc);
        dialog.showAndWait();

        // TODO: corriger ceci (implémenter la modification effective dans le modèle)
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
                // Supprimer des structures locales
                this.tableNodes.remove(tc.getTable().getName());
                this.tcList.remove(tc);
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
        // TODO:
        // try {
        //     DatabaseSchema mld = this.conceptualSchema.transformToDatabase();
        //     if (mld == null) {
        //         CanvasController.showWarningAlert("Erreur", "La conversion a retourné null. Vérifiez l'implémentation de transformToDatabase().");
        //         return;
        //     }

        //     CanvasController.showWarningAlert("Conversion réussie (MCD → MLD)",
        //             "Le MCD a été converti en MLD avec succès !\n\n" +
        //             "Tables créées : " + mld.getTables().size() + "\n\n" +
        //             "Vous pouvez maintenant basculer vers la vue MLD.");
            
        //     MainApp.setSchema(mld);
        // } catch (Exception e) {
        //     MainApp.getLogger().severe(e.getMessage());
        //     CanvasController.showWarningAlert("Erreur", "Erreur lors de la conversion : " + e.getMessage());
        // }
    }
}