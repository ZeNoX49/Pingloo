package com.dbeditor.controller.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.dbeditor.MainApp;
import com.dbeditor.controller.ViewType;
import com.dbeditor.controller.modifier.Drag;
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
import javafx.scene.shape.Rectangle;

public class SdfController extends View {

    private class LabelSdf extends Label implements Visual, Drag {

        public LabelSdf(String text) {
            super(text);
        }

        // callbacks fournis par CanvasController
        private BiConsumer<Drag, MouseEvent> onSelect;
        private BiConsumer<Drag, MouseEvent> onDrag;
        private BiConsumer<Drag, MouseEvent> onDragEnd;

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
                // TODO
                // this.setStyle(this.getStyle() + "; -fx-border-color: " + T_M.getTheme().getSelectionBorderColor() + ";");
            } else {
                this.updateStyle(); // remet le style normal
            }
        }

        @Override
        public void setOnSelect(BiConsumer<Drag, MouseEvent> onSelect) {
            this.onSelect = onSelect;
        }
        @Override
        public void setOnDrag(BiConsumer<Drag, MouseEvent> onDrag) {
            this.onDrag = onDrag;
        }
        @Override
        public void setOnDragEnd(BiConsumer<Drag, MouseEvent> onDragEnd) {
            this.onDragEnd = onDragEnd;
        }

        @Override
        public LabelSdf getRoot() { return this; }

        @Override
        public void updateStyle() {
            // TODO
        }

        @Override
        public void updateType() {}

    }

    private class Connection {
        public final LabelSdf from;
        public final LabelSdf to;
        public final Line line;
        
        public Connection(LabelSdf from, LabelSdf to, Line line) {
            this.from = from;
            this.to = to;
            this.line = line;
        }
    }

    private static final ThemeManager T_M = ThemeManager.getInstance();

    // Nodes visuels
    private final Map<String, Drag> labels = new HashMap<>();
    private final List<Connection> arrows = new ArrayList<>();

    // Helpers
    private ZoomPanHandler zoomPan;
    private SelectionModel selectionModel;
    private LassoSelector lasso;
    private MultiDragManager multiDrag;

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
        this.selectionModel = new SelectionModel((tc, selected) -> tc.setSelected(selected));
        
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
        this.lasso = new LassoSelector(this.pane, this.group, this.labels, this.selectionModel);
        this.lasso.setupEvents();

        this.updateStyle();
    }

    @Override
    public void open() {
        // supprime tous les nodes sauf selectionRect
        this.group.getChildren().removeIf(node -> node != this.lasso.rect);

        // vider les structures
        this.labels.clear();
        this.arrows.clear();

        // créer les nodes
        // this.createTableNodes();
        for(Table t : MainApp.schema.getTables()) {
            List<Column> keys = new ArrayList<>();
            List<Column> columns = new ArrayList<>();

            for(Column c : t.getColumns()) {
                if(c.isPrimaryKey) {
                    keys.add(c);
                } else {
                    columns.add(c);
                }

                String name = this.getLabelText(t, c);
                LabelSdf ls = new LabelSdf(name);
                ls.setOnSelect((d, e) -> this.handleSelection(d, e));
                this.labels.put(name, ls);
                this.group.getChildren().add(ls);
                this.multiDrag.attach(ls);
            }

            for(Column k : keys) {
                for(Column c : columns) {
                    LabelSdf lk = this.getLabelSdf(this.getLabelText(t, k));
                    LabelSdf lc = this.getLabelSdf(this.getLabelText(t, c));
                    this.drawArrows(lk, lc);
                }
            }
        }
        
        // this.drawLinks();
        for(Table t : MainApp.schema.getTables()) {
            for(ForeignKey fk : t.getForeignKeys()) {
                String from = fk.referencedTable+"-"+fk.referencedColumn;
                LabelSdf lf = this.getLabelSdf(from);

                for(Column c : t.getColumns()) {
                    LabelSdf lt = this.getLabelSdf(this.getLabelText(t, c));
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

    private void drawArrows(LabelSdf from, LabelSdf to) {
        if(from == null || to == null) return;

        double fromX = from.getRoot().getLayoutX() + from.getRoot().getWidth() / 2;
        double fromY = from.getRoot().getLayoutY() + from.getRoot().getHeight() / 2;
        double toX = to.getRoot().getLayoutX() + to.getRoot().getWidth() / 2;
        double toY = to.getRoot().getLayoutY() + to.getRoot().getHeight() / 2;

        Line line = new Line(fromX, fromY, toX, toY);
        line.setStroke(Color.web(T_M.getTheme().getSecondaryTextColor()));
        line.setStrokeWidth(2);
        line.getStrokeDashArray().addAll(5.0, 5.0);
        
        line.startXProperty().bind(from.getRoot().layoutXProperty().add(from.getRoot().widthProperty().divide(2)));
        line.startYProperty().bind(from.getRoot().layoutYProperty().add(from.getRoot().heightProperty().divide(2)));
        line.endXProperty().bind(to.getRoot().layoutXProperty().add(to.getRoot().widthProperty().divide(2)));
        line.endYProperty().bind(to.getRoot().layoutYProperty().add(to.getRoot().heightProperty().divide(2)));
    }

    @Override
    public void updateStyle() {
        // TODO
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
    private void handleSelection(Drag label, MouseEvent e) {
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

    private LabelSdf getLabelSdf(String name) {
        Drag d = this.labels.get(name);
        if(d != null && d instanceof LabelSdf ls) {
            return ls;
        }
        return null;
    }
    
}