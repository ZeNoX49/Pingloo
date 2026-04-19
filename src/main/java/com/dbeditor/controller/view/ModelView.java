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
import com.dbeditor.sql.DbType;
import com.dbeditor.util.ThemeManager;

import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public abstract class ModelView extends View {

    protected class Connection {
        public final String firstTable;
        public final String secondTable;
        public final Line line;
        public final Label label;

        /**
         * pour le MCD : entité, association, ...
         */
        public Connection(String firstTable, String secondTable, Line line, Label label) {
            this.firstTable = firstTable;
            this.secondTable = secondTable;
            this.line = line;
            this.label = label;
        }

        public boolean involves(String tableName) {
            return this.firstTable.equals(tableName) || this.secondTable.equals(tableName);
        }

        public void removeFrom(Group group) {
            group.getChildren().removeAll(this.line, this.label);
        }
    }

    private static final ThemeManager T_M = ThemeManager.getInstance();

    // Nodes visuels
    protected final Map<String, TableController> tableNodes = new HashMap<>();
    protected final List<Connection> connectionLines = new ArrayList<>();

    // Helpers
    protected ZoomPanHandler zoomPan;
    protected SelectionModel<TableController> selectionModel;
    protected LassoSelector lasso;
    protected MultiDragManager multiDrag;

    protected Label zlLabel;
    private Pane pane;
    protected Group group;

    @Override
    public void initialization(ToolBar toolbar) {
        this.zlLabel = new Label("");
        toolbar.getItems().add(this.zlLabel);

        this.pane = new Pane();
        this.group = new Group();
        this.pane.getChildren().add(this.group);
        
        // Initialiser le modèle de sélection -> visualizer appelle setSelected sur TableController
        this.selectionModel = new SelectionModel<>((tc, selected) -> tc.setSelected(selected));
        
        // Initialiser le zoom/pan
        this.zoomPan = new ZoomPanHandler(this.pane, this.group);
        this.zoomPan.setupEvents(this.zlLabel);

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
        
        this.zlLabel.setStyle("-fx-text-fill: " + T_M.getTheme().getTextColor() + ";");
        
        for (TableController tc : this.tableNodes.values()) {
            tc.updateStyle();
        }

        for(Connection c : this.connectionLines) {
            if(c.label == null) continue;
            c.label.setStyle(
                "-fx-background-color: " + T_M.getTheme().getBackgroundColor() + "; " + 
                "-fx-text-fill: " + T_M.getTheme().getTextColor() + ";" +
                "-fx-font-size: 15;"
            );
        }
    }

    @Override
    public void updateType() {
        for (TableController tc : this.tableNodes.values()) {
            tc.updateType();
        }
    }

    /**
     * Gère la sélection d'une entité
     */
    protected void handleSelection(TableController table, MouseEvent e) {
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

    @Override
    public Pane getRoot() {
        return this.pane;
    }
}