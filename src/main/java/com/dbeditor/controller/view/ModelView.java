package com.dbeditor.controller.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dbeditor.controller.TableController;
import com.dbeditor.controller.view.helpers.LassoSelector;
import com.dbeditor.controller.view.helpers.MultiDragManager;
import com.dbeditor.controller.view.helpers.SelectionModel;
import com.dbeditor.controller.view.helpers.ZoomPanHandler;
import com.dbeditor.util.ThemeManager;

import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

public abstract class ModelView extends View {
    private static final ThemeManager T_M = ThemeManager.getInstance();

    // Nodes visuels
    private final Map<String, TableController> tableNodes = new HashMap<>();
    private final List<Pair<Line, Label>> connectionLines = new ArrayList<>();

    // Helpers
    private ZoomPanHandler zoomPan;
    private SelectionModel<TableController> selectionModel;
    private LassoSelector lasso;
    private MultiDragManager multiDrag;

    private Label zlLabel;
    private Pane pane;
    private Group group;

    @Override
    public void initialization(ToolBar toolbar) {
        this.zlLabel = new Label("");
        // toolbar.getChildrenUnmodifiable().add(this.zlLabel);

        this.pane = new Pane();
        this.group = new Group();
        this.pane.getChildren().add(this.group);

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
        this.lasso = new LassoSelector(this.pane, this.group, this.tableNodes, this.selectionModel);
        this.lasso.setupEvents();

        this.updateStyle();
    }

    @Override
    public void updateStyle() {
        this.pane.setStyle("-fx-background-color: " + T_M.getTheme().getBackgroundColor() + ";");
        
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

    /* ========================= Utilitaire  ========================= */
    @Override
    public Pane getRoot() {
        return this.pane;
    }

    public Map<String, TableController> getTableNodes() { return this.tableNodes; }
    public List<Pair<Line, Label>> getConnectionLines() { return this.connectionLines; }
    public ZoomPanHandler getZoomPan() { return this.zoomPan; }
    public SelectionModel<TableController> getSelectionModel() { return this.selectionModel; }
    public LassoSelector getLasso() { return this.lasso; }
    public MultiDragManager getMultiDrag() { return this.multiDrag; }
    public Label getZlLabel() { return this.zlLabel; }
    public Pane getPane() { return this.pane; }
    public Group getGroup() { return this.group; }
}