package com.dbeditor.controller;

import java.util.function.BiConsumer;

import com.dbeditor.model.Attribute;
import com.dbeditor.model.Entity;
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

/**
 * Contrôleur pour afficher une entité dans le MCD
 * Style : rectangle avec attributs listés (identifiant souligné)
 */
public class EntityController {
    private static final ThemeManager T_M = ThemeManager.getInstance();

    @FXML private AnchorPane pane;
    @FXML private HBox hName;
    @FXML private Label name;
    @FXML private GridPane grid;

    private Entity entity;

    // callbacks fournis par McdController
    private BiConsumer<EntityController, MouseEvent> onSelect;
    private BiConsumer<EntityController, MouseEvent> onDrag;
    private BiConsumer<EntityController, MouseEvent> onDragEnd;

    public void createEntityNode(Entity entity) {
        this.entity = entity;
        this.createUI();
        this.setupDragHandlers();
    }
    
    /**
     * Permet de créer l'UI de l'entité (style MCD Merise)
     */
    private void createUI() {
        this.hName.setPadding(new Insets(5));

        // Nom de l'entité en gras
        this.name.setText(this.entity.getName());
        this.name.setFont(Font.font("System", FontWeight.BOLD, 22));
        this.name.setTextFill(Color.WHITE);

        this.grid.setPadding(new Insets(5));
        this.grid.setVgap(3);

        // Afficher les attributs
        for (int i = 0; i < this.entity.getAttributes().size(); i++) {
            Attribute attr = this.entity.getAttributes().get(i);

            Label attrLabel = new Label(attr.getName());
            
            // L'identifiant est souligné
            if (attr.isIdentifier()) {
                attrLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
                attrLabel.setUnderline(true);
            } else {
                attrLabel.setFont(Font.font("System", 12));
            }
            
            this.grid.add(attrLabel, 0, i);
            GridPane.setMargin(attrLabel, new Insets(1, 5, 1, 5));
        }

        this.updateStyle();
    }
    
    /**
     * Permete de mettre à jour le style
     */
    public void updateStyle() {
        // Style rectangle simple pour entité MCD
        this.pane.setStyle(
            "-fx-background-color: " + T_M.getTheme().getCardColor() + "; " +
            "-fx-border-color: " + T_M.getTheme().getBorderColor() + "; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 0; " + // Rectangle (pas arrondi)
            "-fx-background-radius: 0;"
        );

        this.hName.setStyle(
            "-fx-background-color: " + T_M.getTheme().getHeaderColor() + "; " +
            "-fx-translate-x: 0px; -fx-translate-y: 0px;"
        );

        // Tous les attributs en même couleur
        for (Node node : grid.getChildren()) {
            if (node instanceof Label label) {
                label.setStyle("-fx-text-fill: " + T_M.getTheme().getTextColor() + ";");
            }
        }
    }
    
    /**
     * Met en place la logique de drag
     */
    private void setupDragHandlers() {
        this.pane.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (this.onSelect != null) {
                    this.onSelect.accept(this, e);
                }
                this.pane.toFront();
                e.consume();
            }
        });
        
        this.pane.setOnMouseDragged(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
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
     * Modifie le bord de l'entité en fonction de la sélection
     */
    public void setSelected(boolean selected) {
        if (selected) {
            pane.setStyle(pane.getStyle() + "; -fx-border-color: " + 
                         T_M.getTheme().getSelectionBorderColor() + "; -fx-border-width: 3;");
        } else {
            this.updateStyle();
        }
    }

    public boolean contains(Point2D pInParent) {
        return this.pane.getBoundsInParent().contains(pInParent);
    }

    public void setOnSelect(BiConsumer<EntityController, MouseEvent> onSelect) {
        this.onSelect = onSelect;
    }
    public void setOnDrag(BiConsumer<EntityController, MouseEvent> onDrag) {
        this.onDrag = onDrag;
    }
    public void setOnDragEnd(BiConsumer<EntityController, MouseEvent> onDragEnd) {
        this.onDragEnd = onDragEnd;
    }
    
    public Entity getEntity() { return entity; }
    public AnchorPane getRoot() { return this.pane; }
}