package com.dbeditor.controller.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.dbeditor.MainApp;
import com.dbeditor.controller.TableController;
import com.dbeditor.controller.view.dialogs.TableEditorDialog;
import com.dbeditor.controller.view.helpers.LassoSelector;
import com.dbeditor.controller.view.helpers.MultiDragManager;
import com.dbeditor.controller.view.helpers.SelectionModel;
import com.dbeditor.controller.view.helpers.ZoomPanHandler;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;
import com.dbeditor.util.ThemeManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 * McdController se repose sur des helpers:
 *  - ZoomPanHandler : zoom + pan
 *  - SelectionModel : gestion de la sélection
 *  - LassoSelector : lasso rectangulaire
 *  - MultiDragManager : drag multi objets
 */
public class McdController extends View {
    private static final ThemeManager T_M = ThemeManager.getInstance();

    @FXML private BorderPane root;
    @FXML private ToolBar toolbar;
    @FXML private ComboBox<String> cb;
    @FXML private Button btnTable, btnAssociation, btnLink;
    @FXML private Label zlLabel;
    @FXML private Pane pane;
    @FXML private Group group;

    // modèle de données / noeuds
    private final List<TableController> tableNodes = new ArrayList<>();
    private final List<Line> connectionLines = new ArrayList<>();

    // helpers
    private ZoomPanHandler zoomPan;
    private SelectionModel selectionModel;
    private LassoSelector lasso;
    private MultiDragManager multiDrag;

    // Context menu pour les tables
    private ContextMenu tableContextMenu;

    @FXML
    void initialize() throws IOException {
        // visualizer appelle setSelected sur TableController
        this.selectionModel = new SelectionModel((table, bool) -> table.setSelected(bool));

        this.zoomPan = new ZoomPanHandler(this.pane, this.group);
        this.zoomPan.setupEvents(this.zlLabel);
        this.zlLabel.setText("%.2f".formatted(this.zoomPan.getZoomLevel()));

        this.multiDrag = new MultiDragManager(this.selectionModel);

        // Permet au node de ne pas sortir du pane (pour ne pas les voir au dessus de la toolbar)
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(this.pane.widthProperty());
        clip.heightProperty().bind(this.pane.heightProperty());
        this.pane.setClip(clip);

        this.lasso = new LassoSelector(this.pane, this.group, this.tableNodes, this.selectionModel);
        this.lasso.setupEvents();

        // Initialiser le menu contextuel
        this.initContextMenu();

        this.updateStyle();
    }

    @Override
    public void updateStyle() {
        this.pane.setStyle("-fx-background-color: " + T_M.getTheme().getBackgroundColor() + ";");
        this.toolbar.setStyle(
                "-fx-background-color: " + T_M.getTheme().getToolbarColor() + "; " + 
                "-fx-border-color: " + T_M.getTheme().getToolbarBorderColor() + "; " + 
                "-fx-border-width: 0 0 1 0;"
        );
        
        for (TableController tc : this.tableNodes) {
            tc.updateStyle();
        }

        // Mettre à jour la couleur des lignes
        for (Line line : this.connectionLines) {
            line.setStroke(Color.web(T_M.getTheme().getSecondaryTextColor()));
        }
    }

    @Override
    public void open(DatabaseSchema dbS) throws IOException {
        if (dbS == null) return;

        // supprime tous les nodes sauf selectionRect
        this.group.getChildren().removeIf(node -> node != this.lasso.getRect());

        this.tableNodes.clear();
        this.connectionLines.clear();

        this.createTableNodes(dbS);
        this.drawConnections();

        if (this.lasso != null) {
            this.lasso.getRect().toFront();
        }
    }

    @Override
    public void onChange() {}

    /**
     * Initialise le menu contextuel pour les tables
     */
    private void initContextMenu() {
        this.tableContextMenu = new ContextMenu();
        
        MenuItem editItem = new MenuItem("Modifier");
        editItem.setOnAction(e -> {
            TableController selectedTable = getSelectedTable();
            if (selectedTable != null) {
                editTable(selectedTable);
            }
        });
        
        MenuItem deleteItem = new MenuItem("Supprimer");
        deleteItem.setOnAction(e -> {
            TableController selectedTable = getSelectedTable();
            if (selectedTable != null) {
                deleteTable(selectedTable);
            }
        });
        
        this.tableContextMenu.getItems().addAll(editItem, deleteItem);
    }

    /**
     * Permet de créer le visuel des tables à partir d'un DatabaseSchema
     * @param dbS
     */
    private void createTableNodes(DatabaseSchema dbS) throws IOException {
        int col = 0, row = 0;
        int cols = (int) Math.ceil(Math.sqrt(dbS.getTables().size()));

        for (Table table : dbS.getTables().values()) {
            TableController nodeController = createTableNode(table, col * 350 + 50, row * 250 + 50);
            this.tableNodes.add(nodeController);

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
     * Crée un node de table à une position donnée
     * @param table la table à créer
     * @param x position X
     * @param y position Y
     * @return le contrôleur de la table créée
     */
    private TableController createTableNode(Table table, double x, double y) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/table.fxml"));
        AnchorPane nodePane = loader.load();
        TableController nodeController = loader.getController();
        nodeController.createTableNode(table);

        // gérer la sélection d'un table lorsqu'elle est cliquée
        nodeController.setOnSelect((tc, e) -> handleSelection(tc, e));

        // Ajouter le menu contextuel avec clic droit
        nodePane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                // Sélectionner la table si elle ne l'est pas déjà
                if (!this.selectionModel.contains(nodeController)) {
                    this.selectionModel.clear();
                    this.selectionModel.select(nodeController);
                }
                this.tableContextMenu.show(nodePane, e.getScreenX(), e.getScreenY());
                e.consume();
            }
        });

        // attache le node pour le multidrag
        this.multiDrag.attach(nodeController);

        // position initiale
        nodePane.setLayoutX(x);
        nodePane.setLayoutY(y);

        this.group.getChildren().add(nodePane);
        
        return nodeController;
    }

    /**
     * Tracer tout les liens entre les tables
     */
    private void drawConnections() {
        // Supprimer les anciennes lignes
        this.connectionLines.forEach(line -> this.group.getChildren().remove(line));
        this.connectionLines.clear();

        for (TableController fromNode : this.tableNodes) {
            Table fromTable = fromNode.getTable();
            for (ForeignKey fk : fromTable.getForeignKeys()) {
                TableController toNode = this.findTableNode(fk.getReferencedTable());
                if (toNode != null) {
                    this.drawConnection(fromNode, toNode);
                }
            }
        }
    }

    /**
     * Permet de tracer un lien entre 2 tables
     * @param from -> table 1
     * @param to -> table 2
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
     * Trouver une table avec son nom
     * @param tableName -> nom de la table
     * @return table trouvée, null sinon
     */
    private TableController findTableNode(String tableName) {
        for (TableController tc : tableNodes) {
            if (tc.getTable().getName().equals(tableName)) {
                return tc;
            }
        }
        return null;
    }

    /**
     * S'occupe de gérer la sélection d'une table
     * @param table
     * @param e
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

    /**
     * Récupère la table sélectionnée (la première si plusieurs)
     */
    private TableController getSelectedTable() {
        List<TableController> selected = this.selectionModel.getSelected();
        return selected.isEmpty() ? null : selected.get(0);
    }

    /**
     * Ajoute une nouvelle table
     */
    @FXML
    private void addTable() {
        DatabaseSchema schema = MainApp.getSchema();
        if (schema == null) {
            showAlert("Erreur", "Aucun schéma de base de données n'est ouvert.");
            return;
        }

        // Ouvrir le dialogue d'édition
        TableEditorDialog dialog = new TableEditorDialog();
        dialog.showAndWait();

        if (dialog.isConfirmed()) {
            Table newTable = dialog.getResultTable();
            
            // Vérifier si une table avec ce nom existe déjà
            if (schema.getTable(newTable.getName()) != null) {
                showAlert("Erreur", "Une table avec ce nom existe déjà.");
                return;
            }

            // Ajouter la table au schéma
            schema.addTable(newTable);

            try {
                // Créer le node visuel
                // Position au centre de la vue visible
                double x = (this.pane.getWidth() / 2) - 150;
                double y = (this.pane.getHeight() / 2) - 100;
                
                TableController newNode = createTableNode(newTable, x, y);
                this.tableNodes.add(newNode);

                // Sélectionner la nouvelle table
                this.selectionModel.clear();
                this.selectionModel.select(newNode);

                // Mettre le rectangle de sélection devant
                if (this.lasso != null) {
                    this.lasso.getRect().toFront();
                }

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de créer la table visuellement.");
            }
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
                showAlert("Erreur", "Une table avec ce nom existe déjà.");
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
                showAlert("Erreur", "Impossible de mettre à jour la table.");
            }
        }
    }

    /**
     * Supprime une table
     * @param tableController le contrôleur de la table à supprimer
     */
    private void deleteTable(TableController tableController) {
        DatabaseSchema schema = MainApp.getSchema();
        if (schema == null) return;

        Table table = tableController.getTable();

        // Demander confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la table");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer la table '" + table.getName() + "' ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Supprimer du schéma
            schema.removeTable(table.getName());

            // Supprimer le node visuel
            this.group.getChildren().remove(tableController.getRoot());
            this.tableNodes.remove(tableController);

            // Supprimer de la sélection
            this.selectionModel.deselect(tableController);

            // Redessiner les connexions (au cas où cette table était référencée)
            this.drawConnections();
        }
    }

    /**
     * Supprime les tables sélectionnées (touche Suppr)
     */
    public void deleteSelectedTables() {
        List<TableController> selected = new ArrayList<>(this.selectionModel.getSelected());
        
        if (selected.isEmpty()) return;

        // Demander confirmation
        String message = selected.size() == 1 
            ? "Êtes-vous sûr de vouloir supprimer la table '" + selected.get(0).getTable().getName() + "' ?"
            : "Êtes-vous sûr de vouloir supprimer " + selected.size() + " tables ?";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer les tables");
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DatabaseSchema schema = MainApp.getSchema();
            if (schema == null) return;

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
     * Affiche une alerte
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void addAssociation() {
        // TODO: implémenter plus tard
        showAlert("Fonctionnalité à venir", "L'ajout d'associations sera implémenté prochainement.");
    }
    
    @FXML
    private void addLink() {
        // TODO: implémenter plus tard
        showAlert("Fonctionnalité à venir", "L'ajout de liens sera implémenté prochainement.");
    }

    /**
     * Getter pour permettre l'accès depuis l'extérieur (ex: raccourci clavier)
     */
    public Pane getPane() {
        return this.pane;
    }
}