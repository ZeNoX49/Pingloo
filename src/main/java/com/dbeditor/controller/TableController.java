package com.dbeditor.controller;

import java.util.function.BiConsumer;

import com.dbeditor.controller.modifier.Draggable;
import com.dbeditor.controller.modifier.Visual;
import com.dbeditor.manager.Singleton;
import com.dbeditor.manager.ThemeManager;
import com.dbeditor.model.Attribute;
import com.dbeditor.model.Column;
import com.dbeditor.model.Table;

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

public class TableController implements Visual, Draggable<TableController> {

    public enum TableType {
        // Entity, Association, Table;
        Entity, Association;
    }

    private static final ThemeManager T_M = Singleton.getInstance(ThemeManager.class);

    @FXML private AnchorPane pane;
    @FXML private Ellipse ellipse;
    @FXML private HBox hName;
    @FXML private Label name;
    @FXML private GridPane grid;

    private final Table table;
    private final TableType type;

    // callbacks fournis par CanvasController
    private BiConsumer<TableController, MouseEvent> onSelect;
    private BiConsumer<TableController, MouseEvent> onDrag;
    private BiConsumer<TableController, MouseEvent> onDragEnd;

    public TableController(Table table, TableType type) {
        this.table = table;
        this.type = type;
    }

    @FXML
    private void initialize() {
        this.createUI();
        this.setupDragHandlers();
    }

    /**
     * permet de recharger les données de la table après une modification
     */
    public void reload() {
        this.grid.getChildren().clear();
        this.initialize();
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

        int i = 0;
        // TODO: if (this.type == TableType.Table) {
        //     for (Entry<String, Boolean> entry : this.table.getAttributs().entrySet()) {
        //         if (entry.getValue()) {
        //             Column col = this.table.columns.get(entry.getKey());
        //             this.createGridLine(i++, col, col.type);
        //         } else {
        //             ForeignKey fk = this.table.foreignKeys.get(entry.getKey());
        //             this.createGridLine(i++, fk, fk.referencedColumn.type);
        //         }
        //     }
        // } else {
        for (Column col : this.table.getColumns()) {
            this.createGridLine(i++, col, col.type);
        }
        // }

        this.updateStyle();
    }

    private void createGridLine(int i, Attribute attr, String type) {
        Label colName = new Label(attr.name);
        if (attr.isPrimaryKey || attr.isUnique) {
            colName.setFont(Font.font("System", FontWeight.BOLD, 12));
            // uniquement pour les clés primaire
            if (attr.isPrimaryKey) {
                colName.setUnderline(true);
            }
        } else {
            colName.setFont(Font.font("System", 12));
        }
        this.grid.add(colName, 0, i);
        GridPane.setMargin(colName, new Insets(1, 1, 1, 3));

        Label colType = new Label(type);
        if (attr.isPrimaryKey || attr.isNotNull) {
            colType.setFont(Font.font("System", FontWeight.BOLD, 11));
        } else {
            colType.setFont(Font.font("System", 11));
        }
        this.grid.add(colType, 1, i);
        GridPane.setMargin(colType, new Insets(1, 3, 1, 1));
    }

    @Override
    public void updateStyle() {
        if (this.type == TableType.Association) {
            this.pane.setStyle("-fx-background-color: transparent");
            this.ellipse.setStyle(
                    "-fx-background-color: " + T_M.getTheme().getCardColor() + "; "
                    + "-fx-border-color: " + T_M.getTheme().getBorderColor() + "; "
                    + "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);"
            );
            this.hName.setStyle(
                    "-fx-translate-x: 1px; -fx-translate-y: 1px;"
            );
        } else {
            this.pane.setStyle(
                    "-fx-background-color: " + T_M.getTheme().getCardColor() + "; "
                    + "-fx-border-color: " + T_M.getTheme().getBorderColor() + "; "
                    // TODO: TableType.Table -> + (this.type.equals(TableType.Table) ? "-fx-border-radius: 8; -fx-background-radius: 8; " : "")
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);"
            );
            this.ellipse.setStyle("-fx-background-color: transparent");
            this.hName.setStyle(
                    "-fx-background-color: " + T_M.getTheme().getHeaderColor() + "; "
                    // TODO: TableType.Table -> + (this.type.equals(TableType.Table) ? "-fx-background-radius: 8 8 0 0; " : "")
                    + "-fx-translate-x: 1px; -fx-translate-y: 1px;"
            );
        }

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

    // @Override
    // public void updateType() {
    //     // TODO
    // }

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
                if (this.onDragEnd != null) {
                    this.onDragEnd.accept(this, e);
                }
                e.consume();
            }
        });
    }

    @Override
    public void setSelected(boolean selected) {
        if (selected) {
            this.pane.setStyle(this.pane.getStyle() + "; -fx-border-color: " + T_M.getTheme().getSelectionBorderColor() + ";");
        } else {
            this.updateStyle(); // remet le style normal
        }
    }

    @Override
    public void setOnSelect(BiConsumer<TableController, MouseEvent> onSelect) {
        this.onSelect = onSelect;
    }

    @Override
    public void setOnDrag(BiConsumer<TableController, MouseEvent> onDrag) {
        this.onDrag = onDrag;
    }

    @Override
    public void setOnDragEnd(BiConsumer<TableController, MouseEvent> onDragEnd) {
        this.onDragEnd = onDragEnd;
    }

    @Override
    public AnchorPane getRoot() {
        return this.pane;
    }

    public Table getTable() {
        return this.table;
    }

    public TableType getType() {
        return this.type;
    }

}
