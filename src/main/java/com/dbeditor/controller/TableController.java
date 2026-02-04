package com.dbeditor.controller;

import java.util.function.BiConsumer;

import com.dbeditor.model.Column;
import com.dbeditor.model.Table;
import com.dbeditor.util.ThemeManager;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class TableController {
    private static final ThemeManager T_M = ThemeManager.getInstance();

    @FXML private AnchorPane pane;
    @FXML private HBox hName;
    @FXML private Label name;
    @FXML private GridPane grid;

    private Table table;

    // callbacks fournis par CanvasController
    private BiConsumer<TableController, MouseEvent> onSelect;
    private BiConsumer<TableController, MouseEvent> onDrag;
    private BiConsumer<TableController, MouseEvent> onDragEnd;

    public void createTableNode(Table table) {
        this.table = table;
        this.createUI();
        this.setupDragHandlers();
    }
    
    /**
     * Permet de créer l'UI de la table
     */
    private void createUI() {
        this.hName.setPadding(new Insets(2));

        this.name.setText(this.table.getName());
        this.name.setFont(Font.font("System", FontWeight.BOLD, 24));
        this.name.setTextFill(Color.WHITE);

        this.grid.setPadding(new Insets(2));

        for (int i = 0; i < this.table.getColumns().size(); i++) {
            Column col = this.table.getColumns().get(i);

            Label colName = new Label(col.getName());
            if(col.isPrimaryKey() || col.isUnique()) {
                colName.setFont(Font.font("System", FontWeight.BOLD, 12));
                // uniquement pour les clés primaire
                if(!col.isUnique()) colName.setUnderline(true);
            } else {
                colName.setFont(Font.font("System", 12));
            }
            this.grid.add(colName, 0, i);
            GridPane.setMargin(colName, new Insets(1, 1, 1, 3));
            
            Label colType = new Label(col.getType());
            if(col.isPrimaryKey() || col.isNotNull()) {
                colType.setFont(Font.font("System", FontWeight.BOLD, 11));
            } else {
                colType.setFont(Font.font("System", 11));
            }
            this.grid.add(colType, 1, i);
            GridPane.setMargin(colType, new Insets(1, 3, 1, 1));
        }

        this.updateStyle();
    }
    
    /**
     * Permete de mettre à jour le style au lancement de l'app
     * ou lors d'un changement de style
     */
    public void updateStyle() {
        this.pane.setStyle("-fx-background-color: " + T_M.getTheme().getCardColor() + 
                "; -fx-border-color: " + T_M.getTheme().getBorderColor() + 
                "; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");

        this.hName.setStyle("-fx-background-color: " + T_M.getTheme().getHeaderColor() + 
                    "; -fx-background-radius: 8 8 0 0;" + 
                    "-fx-translate-x: 1px; -fx-translate-y: 1px;");

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
    
    /**
     * Met en place la logique de drag
     */
    private void setupDragHandlers() {
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

    /**
     * Modifie le bord de la table en fonction de la sélection
     * @param selected
     */
    public void setSelected(boolean selected) {
        if (selected) {
            pane.setStyle(pane.getStyle() + "; -fx-border-color: " + T_M.getTheme().getSelectionBorderColor() + ";");
        } else {
            this.updateStyle(); // remet le style normal
        }
    }

    /**
     * On regarde si l'élement est déja inclus
     * @param pInParent
     * @return
     */
    public boolean contains(Point2D pInParent) {
        return this.pane.getBoundsInParent().contains(pInParent);
    }

    public void setOnSelect(BiConsumer<TableController, MouseEvent> onSelect) {
        this.onSelect = onSelect;
    }
    public void setOnDrag(BiConsumer<TableController, MouseEvent> onDrag) {
        this.onDrag = onDrag;
    }
    public void setOnDragEnd(BiConsumer<TableController, MouseEvent> onDragEnd) {
        this.onDragEnd = onDragEnd;
    }
    
    public Table getTable() { return table; }
    public AnchorPane getRoot() { return this.pane; }
}
