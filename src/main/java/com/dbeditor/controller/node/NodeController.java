package com.dbeditor.controller.node;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.dbeditor.controller.modifier.Draggable;
import com.dbeditor.controller.modifier.Visual;
import com.dbeditor.model.Column;
import com.dbeditor.model.ConceptualNode;
import com.dbeditor.sql.DbType;
import com.dbeditor.util.ThemeManager;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public abstract class NodeController implements Visual, Draggable {

    private static final ThemeManager T_M = ThemeManager.getInstance();

    @FXML protected AnchorPane pane;
    @FXML protected Ellipse ellipse;
    @FXML protected HBox hName;
    @FXML protected Label name;
    @FXML protected GridPane grid;

    protected ConceptualNode table;

    private BiConsumer<Draggable, MouseEvent> onSelect;
    private BiConsumer<Draggable, MouseEvent> onDrag;
    private BiConsumer<Draggable, MouseEvent> onDragEnd;

    /**
     * Permet de mettre en place le visuel de la table.<br>
     * Met aussi en en place l'ui et le drag
     */
    protected void createNodeController(ConceptualNode table) {
        this.table = table;
        this.createUI();
        this.setupDragHandlers();
    }
    
    /**
     * Permet de créer l'UI de la table
     */
    private void createUI() {
        this.pane.setLayoutX(table.getPosX());
        this.pane.setLayoutY(table.getPosY());

        this.hName.setPadding(new Insets(2));

        this.name.setText(this.table.name);
        this.name.setFont(Font.font("System", FontWeight.BOLD, 24));
        this.name.setTextFill(Color.WHITE);

        this.grid.setPadding(new Insets(2));

        List<Column> columns = new ArrayList<>(this.table.columns.values());
        for (int i = 0; i < columns.size(); i++) {
            Column col = columns.get(i);

            // if(this.type == TableType.Association && col.isPrimaryKey) continue;
            
            Label colName = new Label(col.name);
            if(col.isPrimaryKey || col.isUnique) {
                colName.setFont(Font.font("System", FontWeight.BOLD, 12));
                // uniquement pour les clés primaire
                if(!col.isUnique) colName.setUnderline(true);
            } else {
                colName.setFont(Font.font("System", 12));
            }
            this.grid.add(colName, 0, i);
            GridPane.setMargin(colName, new Insets(1, 1, 1, 3));
            
            Label colType = new Label(col.type.getRepr(DbType.MySql));
            if(col.isPrimaryKey || col.isNotNull) {
                colType.setFont(Font.font("System", FontWeight.BOLD, 11));
            } else {
                colType.setFont(Font.font("System", 11));
            }
            this.grid.add(colType, 1, i);
            GridPane.setMargin(colType, new Insets(1, 3, 1, 1));
        }

        this.updateStyle();
    }
    
    @Override
    public void updateStyle() {
        this.updateStyleType();

        for (Node node : grid.getChildren()) {
            if (node instanceof Label label) {
                if (GridPane.getColumnIndex(node) == 0) {
                    label.setStyle("-fx-text-fill: " + T_M.getTheme().getTextColor() + ";");
                } else {
                    label.setStyle("-fx-text-fill: " + T_M.getTheme().getSecondaryTextColor() + ";");
                }
            }
        }
    }

    protected abstract void updateStyleType();

    @Override
    public void updateType() {
        // TODO
    }

    @Override
    public void setupDragHandlers() {
        this.pane.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                // informer CanvasController de la sélection
                if (this.onSelect != null) {
                    this.onSelect.accept(this, e);
                }

                // amener au front pour l'UX
                this.pane.toFront();

                e.consume();
            }
        });
        
        this.pane.setOnMouseDragged(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                // déléguer le drag au CanvasController
                if (this.onDrag != null) {
                    this.onDrag.accept(this, e);
                }
                e.consume();
            }
        });

        this.pane.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (this.onDragEnd != null) this.onDragEnd.accept(this, e);
                e.consume();
            }
        });
    }

    @Override
    public void setSelected(boolean selected) {
        if (selected) {
            pane.setStyle(pane.getStyle() + "; -fx-border-color: " + T_M.getTheme().getSelectionBorderColor() + ";");
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
    public AnchorPane getRoot() { return this.pane; }
}
