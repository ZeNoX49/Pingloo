package com.dbeditor.controller.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.dbeditor.MainApp;
import com.dbeditor.controller.ViewType;
import com.dbeditor.controller.modifier.Draggable;
import com.dbeditor.controller.modifier.Visual;
import com.dbeditor.controller.view.helpers.LassoSelector;
import com.dbeditor.controller.view.helpers.MultiDragManager;
import com.dbeditor.controller.view.helpers.SelectionModel;
import com.dbeditor.controller.view.helpers.ZoomPanHandler;
import com.dbeditor.model.Column;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;
import com.dbeditor.util.ThemeManager;

import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

public class SdfController extends View {

    private class LabelSdf extends Label implements Visual, Draggable {

        public LabelSdf(String text) {
            super(text);
        }

        // callbacks fournis par CanvasController
        private BiConsumer<Draggable, MouseEvent> onSelect;
        private BiConsumer<Draggable, MouseEvent> onDrag;
        private BiConsumer<Draggable, MouseEvent> onDragEnd;

        @Override
        public void setupDragHandlers() {
            this.setOnMousePressed(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    if (this.onSelect != null) {
                        this.onSelect.accept(this, e);
                    }

                    // amener au front pour l'UX
                    this.toFront();

                    e.consume();
                }
            });
            
            this.setOnMouseDragged(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    // déléguer le drag
                    if (this.onDrag != null) {
                        this.onDrag.accept(this, e);
                    }
                    e.consume();
                }
            });

            this.setOnMouseReleased(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    if (this.onDragEnd != null) this.onDragEnd.accept(this, e);
                    e.consume();
                }
            });
        }

        @Override
        public void setSelected(boolean selected) {
            if (selected) {
                this.setStyle(this.getStyle() + "-fx-text-fill: " + T_M.getTheme().getSelectionBorderColor() + ";");
            } else {
                this.updateStyle(); // remet le style normal
            }
        }

        @Override
        public void setOnSelect(BiConsumer<Draggable, MouseEvent> onSelect) {
            this.onSelect = onSelect;
        }
        @Override
        public void setOnDrag(BiConsumer<Draggable, MouseEvent> onDrag) {
            this.onDrag = onDrag;
        }
        @Override
        public void setOnDragEnd(BiConsumer<Draggable, MouseEvent> onDragEnd) {
            this.onDragEnd = onDragEnd;
        }

        @Override
        public LabelSdf getRoot() { return this; }

        @Override
        public void updateStyle() {
            this.setStyle(
                "-fx-text-fill: " + T_M.getTheme().getTextColor() + "; " +
                "-fx-background-color: " + T_M.getTheme().getBackgroundColor() + ";"
            );
        }

        @Override
        public void updateType() {}

    }

    private static final ThemeManager T_M = ThemeManager.getInstance();

    // Nodes visuels
    private final Map<String, LabelSdf> labels = new HashMap<>();

    // Helpers
    private ZoomPanHandler zoomPan;
    private SelectionModel<LabelSdf> selectionModel;
    private LassoSelector<LabelSdf> lasso;
    private MultiDragManager<LabelSdf> multiDrag;

    private Label zlLabel;
    private Pane pane;
    private Group group;

    @Override
    public ViewType getViewType() {
        return ViewType.SDF;
    }

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
        this.multiDrag = new MultiDragManager<>(this.selectionModel);

        // Permet au node de ne pas sortir du pane (pour ne pas les voir au dessus de la toolbar)
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(this.pane.widthProperty());
        clip.heightProperty().bind(this.pane.heightProperty());
        this.pane.setClip(clip);

        // Initialiser le lasso avec la liste partagée (vide pour l'instant)
        this.lasso = new LassoSelector<>(this.pane, this.group, this.labels, this.selectionModel);
        this.lasso.setupEvents();

        this.updateStyle();
    }

    @Override
    public void open() {
        // supprime tous les nodes sauf selectionRect
        this.group.getChildren().removeIf(node -> node != this.lasso.rect);

        // vider les structures
        this.labels.clear();

        // créer les nodes et liens des tables
        List<Column> keys = new ArrayList<>();
        List<Column> columns = new ArrayList<>();
        
        for(Table t : MainApp.schema.getTables()) {
            for(Column c : t.getColumns()) {
                if(c.isPrimaryKey) {
                    keys.add(c);
                } else {
                    columns.add(c);
                }
            }

            for(int i = 0; i < keys.size(); i++) {
                double x = t.getPosX() + i * 100 - keys.size() * 50;
                this.createLabelSdf(this.getLabelText(t, keys.get(i)), x, t.getPosY());
            }

            for(int i = 0; i < columns.size(); i++) {
                double x = t.getPosX() + i * 100 - columns.size() * 50;
                this.createLabelSdf(this.getLabelText(t, columns.get(i)), x, t.getPosY() + 100);
            }

            for(Column k : keys) {
                for(Column c : columns) {
                    LabelSdf lk = this.labels.get(this.getLabelText(t, k));
                    LabelSdf lc = this.labels.get(this.getLabelText(t, c));
                    this.drawArrows(lk, lc);
                }
            }

            keys.clear();
            columns.clear();
        }
        
        // lien entre les tables
        // TODO: les cercles (attr1, attr2 -> attr3)
        for(Table t : MainApp.schema.getTables()) {
            for(ForeignKey fk : t.getForeignKeys()) {
                LabelSdf lf = this.labels.get(fk.referencedTable+"-"+fk.referencedColumn);

                for(Column c : t.getColumns()) {
                    LabelSdf lt = this.labels.get(this.getLabelText(t, c));
                    this.drawArrows(lf, lt);
                }
            }
        }

        this.lasso.rect.toFront();

        this.updateStyle();
    }

    private String getLabelText(Table t, Column c) {
        return t.name+"-"+c.name;
    }

    private void createLabelSdf(String name, double x, double y) {
        LabelSdf ls = new LabelSdf(name);
        ls.setLayoutX(x);
        ls.setLayoutY(y);
        ls.setupDragHandlers();
        ls.setOnSelect((d, e) -> this.handleSelection((LabelSdf) d, e));
        this.labels.put(name, ls);
        this.group.getChildren().add(ls);
        this.multiDrag.attach(ls);
    }

    private void drawArrows(LabelSdf from, LabelSdf to) {
        if(from == null || to == null) return;

        Line line = new Line();
        line.setStroke(Color.web(T_M.getTheme().getSecondaryTextColor()));
        line.setStrokeWidth(2);

        line.startXProperty().bind(from.layoutXProperty().add(from.widthProperty().divide(2)));
        line.startYProperty().bind(from.layoutYProperty().add(from.heightProperty().divide(2)));
        line.endXProperty().bind(to.layoutXProperty().add(to.widthProperty().divide(2)));
        line.endYProperty().bind(to.layoutYProperty().add(to.heightProperty().divide(2)));

        Polygon arrowHead = new Polygon();
        arrowHead.setFill(Color.web(T_M.getTheme().getSecondaryTextColor()));
        arrowHead.setStroke(Color.web(T_M.getTheme().getSecondaryTextColor()));

        Runnable updateArrow = () -> {
            double fromX = line.getStartX();
            double fromY = line.getStartY();
            double toX = line.getEndX();
            double toY = line.getEndY();

            double angle = Math.atan2(toY - fromY, toX - fromX);

            double arrowLength = 10;
            double arrowAngle = Math.PI / 6;

            double x1 = toX - arrowLength * Math.cos(angle - arrowAngle);
            double y1 = toY - arrowLength * Math.sin(angle - arrowAngle);

            double x2 = toX - arrowLength * Math.cos(angle + arrowAngle);
            double y2 = toY - arrowLength * Math.sin(angle + arrowAngle);

            arrowHead.getPoints().setAll(
                toX, toY,
                x1, y1,
                x2, y2
            );
        };

        line.startXProperty().addListener((obs, oldV, newV) -> updateArrow.run());
        line.startYProperty().addListener((obs, oldV, newV) -> updateArrow.run());
        line.endXProperty().addListener((obs, oldV, newV) -> updateArrow.run());
        line.endYProperty().addListener((obs, oldV, newV) -> updateArrow.run());

        updateArrow.run();

        this.group.getChildren().add(0, arrowHead);
        this.group.getChildren().add(0, line);
    }

    @Override
    public void updateStyle() {
        this.pane.setStyle("-fx-background-color: " + T_M.getTheme().getBackgroundColor() + ";");
        
        this.zlLabel.setStyle("-fx-text-fill: " + T_M.getTheme().getTextColor() + ";");
        
        for (Draggable d : this.labels.values()) {
            if(d instanceof LabelSdf ls) ls.updateStyle();
        }
    }

    @Override
    public void updateType() {}

    @Override
    public Pane getRoot() {
        return this.pane;
    }

    /**
     * Gère la sélection d'une label
     */
    private void handleSelection(LabelSdf label, MouseEvent e) {
        if (e.isControlDown()) {
            this.selectionModel.toggle(label);
            return;
        }

        if (this.selectionModel.contains(label)) {
            // on laisse faire le drag
            label.getRoot().toFront();
            return;
        }
        
        this.selectionModel.clear();
        this.selectionModel.select(label);
    }
    
}