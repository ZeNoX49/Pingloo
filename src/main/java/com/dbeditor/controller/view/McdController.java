package com.dbeditor.controller.view;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.dbeditor.controller.TableController;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;
import com.dbeditor.util.ThemeManager;
import com.dbeditor.controller.view.helpers.ZoomPanHandler;
import com.dbeditor.controller.view.helpers.SelectionModel;
import com.dbeditor.controller.view.helpers.LassoSelector;
import com.dbeditor.controller.view.helpers.MultiDragManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
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

    // helpers
    private ZoomPanHandler zoomPan;
    private SelectionModel selectionModel;
    private LassoSelector lasso;
    private MultiDragManager multiDrag;

    @FXML
    void initialize() throws IOException {
        // // ===== // initialisation UI
        // super.createSplit(this.pane);
        // super.setupCombobowView(this.cb, View.MCD);

        // visualizer appelle setSelected sur TableController
        this.selectionModel = new SelectionModel((table, bool) -> table.setSelected(bool));

        this.zoomPan = new ZoomPanHandler(this.pane, this.group);
        this.zoomPan.setupEvents();
        this.zlLabel.setText("%.2f".formatted(this.zoomPan.getZoomLevel()));

        this.multiDrag = new MultiDragManager(this.selectionModel);

        // Permet au node de ne pas sortir du pane (pour ne pas les voir au dessus de la toolbar)
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(this.pane.widthProperty());
        clip.heightProperty().bind(this.pane.heightProperty());
        this.pane.setClip(clip);

        this.lasso = new LassoSelector(this.pane, this.group, this.tableNodes, this.selectionModel);
        this.lasso.setupEvents();

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
    }

    @Override
    public void open(DatabaseSchema dbS) throws IOException {
        if (dbS == null) return;

        // supprime tous les nodes sauf selectionRect
        this.group.getChildren().removeIf(node -> node != this.lasso.getRect());

        this.tableNodes.clear();

        this.createTableNodes(dbS);
        this.drawConnections();

        if (this.lasso != null) {
            this.lasso.getRect().toFront();
        }
    }

    @Override
    public void onChange() {}

    /**
     * Permet de créer le visuel des tables à partir d'un DatabaseSchema
     * @param dbS
     */
    private void createTableNodes(DatabaseSchema dbS) throws IOException {
        int col = 0, row = 0;
        int cols = (int) Math.ceil(Math.sqrt(dbS.getTables().size()));

        for (Table table : dbS.getTables().values()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/table.fxml"));
            AnchorPane nodePane = loader.load();
            TableController nodeController = loader.getController();
            nodeController.createTableNode(table);

            // gérer la sélection d'un table lorsqu'elle est cliquée
            nodeController.setOnSelect((tc, e) -> handleSelection(tc, e));

            // attache le node pour le multidrag
            this.multiDrag.attach(nodeController);

            // position initiale
            nodePane.setLayoutX(col * 350 + 50);
            nodePane.setLayoutY(row * 250 + 50);

            this.group.getChildren().add(nodePane);
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
     * Tracer tout les liens entre les tables
     */
    private void drawConnections() {
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

    @FXML
    private void addTable() {

    }
    
    @FXML
    private void addAssociation() {

    }
    
    @FXML
    private void addLink() {

    }
    
}