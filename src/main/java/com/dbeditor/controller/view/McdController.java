package com.dbeditor.controller.view;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.dbeditor.controller.TableController;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;
import com.dbeditor.util.ThemeManager;
import com.microsoft.sqlserver.jdbc.dataclassification.Label;
import com.dbeditor.controller.view.helpers.ZoomPanHandler;
import com.dbeditor.controller.view.helpers.SelectionModel;
import com.dbeditor.controller.view.helpers.LassoSelector;
import com.dbeditor.controller.view.helpers.MultiDragManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
 * - ZoomPanHandler : zoom + pan
 * - SelectionModel : gestion de la sélection
 * - LassoSelector : lasso rectangulaire
 * - MultiDragManager : drag multi objets
 */
public class McdController extends View {
    private static final ThemeManager T_M = ThemeManager.getInstance();

    @FXML private BorderPane root;
    @FXML private ToolBar toolbar;
    @FXML private ComboBox<String> cb;
    @FXML private Button btnAssociation, btnEntity, btnLink;
    @FXML private Label zlLabel;
    @FXML private Pane pane;
    @FXML private Group group;

    // modèle de données / noeuds
    private final List<TableController> tableNodes = new ArrayList<>();

    // // helpers
    private ZoomPanHandler zoomPan;
    // private SelectionModel<TableController> selectionModel;
    private LassoSelector lasso;
    // private MultiDragManager multiDrag;

    // selection rect (référencé aussi dans LassoSelector) - créé dans helper, présent dans group
    private Rectangle selectionRect;

    @FXML
    void initialize() throws IOException {
        // // ===== // initialisation UI
        // super.createSplit(this.pane);
        // super.setupCombobowView(this.cb, View.MCD);

        // // ===== // create selection model (visualizer calls setSelected on TableController)
        // selectionModel = new SelectionModel<>((tc, sel) -> tc.setSelected(sel));

        // ===== // create helpers
        zoomPan = new ZoomPanHandler(pane, group);
        zoomPan.setupEvents();
        // this.zlLabel.steText(this.zoomPan.getZoomLevel())

        // // ===== // lasso will be constructed below after selectionRect is created
        // // ===== // multiDrag depends on selectionModel
        // multiDrag = new MultiDragManager(selectionModel);

        // // ===== // clip pane so content doesn't draw outside
        // Rectangle clip = new Rectangle();
        // clip.widthProperty().bind(this.pane.widthProperty());
        // clip.heightProperty().bind(this.pane.heightProperty());
        // this.pane.setClip(clip);

        // // ===== // create selection rect and add to group (LassoSelector expects it there)
        // createSelectionRect();

        // ===== // now install lasso with tableNodes list (initially empty)
        lasso = new LassoSelector(pane, group, tableNodes, selectionModel);
        lasso.setupEvents();

        // // ===== // handlers for resize to keep content constrained if you want (clamp)
        // this.pane.widthProperty().addListener((obs, o, n) -> clampContentPosition());
        // this.pane.heightProperty().addListener((obs, o, n) -> clampContentPosition());

        this.updateStyle();
    }

    // private void createSelectionRect() {
    //     this.selectionRect = new Rectangle();
    //     this.selectionRect.setManaged(false);
    //     this.selectionRect.setMouseTransparent(true);
    //     this.selectionRect.setFill(Color.web("#4A90E235"));
    //     this.selectionRect.setStroke(Color.web("#4A90E2"));
    //     this.selectionRect.setStrokeWidth(1.5);
    //     this.selectionRect.setVisible(false);
    //     this.group.getChildren().add(this.selectionRect);
    // }

    @Override
    public void updateStyle() {
        // this.group.setStyle("-fx-background-color: transparent;"); // group lui-même transparent
        this.pane.setStyle("-fx-background-color: " + T_M.getTheme().getBackgroundColor() + ";");
        this.toolbar.setStyle("-fx-background-color: " + T_M.getTheme().getToolbarColor() +
                "; -fx-border-color: " + T_M.getTheme().getToolbarBorderColor() + "; -fx-border-width: 0 0 1 0;");
        // update tables styles
        for (TableController tc : this.tableNodes) tc.updateStyle();
    }

    @Override
    public void open(DatabaseSchema dbS) throws IOException {
        if (dbS == null) return;

        // unbind/detach old lines if any
        for (Node n : new ArrayList<>(this.group.getChildren())) {
            if (n instanceof Line) {
                // simply remove old lines and keep selectionRect
                this.group.getChildren().remove(n);
            }
        }

        // remove all nodes except selectionRect
        this.group.getChildren().removeIf(node -> node != this.selectionRect);

        this.tableNodes.clear();

        this.createTableNodes(dbS);
        this.drawConnections();

        // if (this.selectionRect != null) this.selectionRect.toFront();
        // clampContentPosition();
    }

    @Override
    public void onChange() {}

    // --- création nodes & branchements ---
    private void createTableNodes(DatabaseSchema dbS) throws IOException {
        int col = 0, row = 0;
        int cols = (int) Math.ceil(Math.sqrt(dbS.getTables().size()));

        for (Table table : dbS.getTables().values()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/table.fxml"));
            AnchorPane nodePane = loader.load();
            TableController nodeController = loader.getController();
            nodeController.createTableNode(table);

            // // setup callbacks (selection logic handled here, drag delegated to multiDrag)
            // nodeController.setOnSelect((tc, e) -> handleSelection(tc, e));

            // // attach multi-drag manager (it will setOnDrag / setOnDragEnd on the controller)
            // multiDrag.attach(nodeController);

            // position initiale
            nodePane.setLayoutX(col * 350 + 50);
            nodePane.setLayoutY(row * 250 + 50);

            // add to scene graph (group)
            this.group.getChildren().add(nodePane);
            this.tableNodes.add(nodeController);

            col++;
            if (col >= cols) {
                col = 0;
                row++;
            }
        }

        // // ensure selectionRect stays above nodes
        // if (selectionRect != null) selectionRect.toFront();
    }

    private void drawConnections() {
        for (TableController fromNode : this.tableNodes) {
            Table fromTable = fromNode.getTable();
            for (ForeignKey fk : fromTable.getForeignKeys()) {
                TableController toNode = this.findTableNode(fk.getReferencedTable());
                if (toNode != null) {
                    drawConnection(fromNode, toNode);
                }
            }
        }
    }

    private void drawConnection(TableController from, TableController to) {
        // compute endpoints based on layoutX / layoutY
        double fromX = from.getRoot().getLayoutX() + from.getRoot().getWidth() / 2;
        double fromY = from.getRoot().getLayoutY() + from.getRoot().getHeight() / 2;
        double toX = to.getRoot().getLayoutX() + to.getRoot().getWidth() / 2;
        double toY = to.getRoot().getLayoutY() + to.getRoot().getHeight() / 2;

        Line line = new Line(fromX, fromY, toX, toY);
        line.setStroke(Color.web(T_M.getTheme().getSecondaryTextColor()));
        line.setStrokeWidth(2);
        line.getStrokeDashArray().addAll(5.0, 5.0);

        // add behind nodes
        this.group.getChildren().add(0, line);
        bindLine(line, from, to);
    }

    private void bindLine(Line line, TableController from, TableController to) {
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

    private TableController findTableNode(String tableName) {
        for (TableController tc : tableNodes) {
            if (tc.getTable().getName().equals(tableName)) return tc;
        }
        return null;
    }

    // // --- sélection ---
    // private void handleSelection(TableController tc, MouseEvent e) {
    //     if (e.isControlDown()) {
    //         selectionModel.toggle(tc);
    //         return;
    //     }
    //     if (selectionModel.contains(tc)) {
    //         // keep selection as-is (so multi-drag can start)
    //         tc.getRoot().toFront();
    //         return;
    //     }
    //     selectionModel.clear();
    //     selectionModel.select(tc);
    // }

    // // --- clamp content position (simple) ---
    // private void clampContentPosition() {
    //     if (group == null || pane == null) return;

    //     Bounds bounds = group.getBoundsInParent();
    //     double paneW = pane.getWidth();
    //     double paneH = pane.getHeight();

    //     double tx = group.getTranslateX();
    //     double ty = group.getTranslateY();

    //     final double minVisible = 16.0;

    //     if (bounds.getMaxX() < minVisible) {
    //         tx += (minVisible - bounds.getMaxX());
    //     }
    //     if (bounds.getMinX() > paneW - minVisible) {
    //         tx -= (bounds.getMinX() - (paneW - minVisible));
    //     }
    //     if (bounds.getMaxY() < minVisible) {
    //         ty += (minVisible - bounds.getMaxY());
    //     }
    //     if (bounds.getMinY() > paneH - minVisible) {
    //         ty -= (bounds.getMinY() - (paneH - minVisible));
    //     }

    //     // apply if changed noticeably
    //     if (Math.abs(tx - group.getTranslateX()) > 0.1) group.setTranslateX(tx);
    //     if (Math.abs(ty - group.getTranslateY()) > 0.1) group.setTranslateY(ty);
    // }
}
