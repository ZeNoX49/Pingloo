package com.dbeditor.controller;

import java.util.function.BiConsumer;

import com.dbeditor.model.Association;
import com.dbeditor.model.Attribute;
import com.dbeditor.util.ThemeManager;

import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Contrôleur pour afficher une association dans le MCD
 * Style : ovale avec nom de l'association (verbe) et éventuellement attributs
 */
public class AssociationController {
    private static final ThemeManager T_M = ThemeManager.getInstance();

    private StackPane root;
    private Ellipse ellipse;
    private VBox contentBox;
    private Label nameLabel;
    
    private Association association;

    // callbacks fournis par McdController
    private BiConsumer<AssociationController, MouseEvent> onSelect;
    private BiConsumer<AssociationController, MouseEvent> onDrag;
    private BiConsumer<AssociationController, MouseEvent> onDragEnd;

    public AssociationController() {
        this.root = new StackPane();
        this.ellipse = new Ellipse();
        this.contentBox = new VBox(2);
        this.contentBox.setAlignment(Pos.CENTER);
        
        this.root.getChildren().addAll(ellipse, contentBox);
    }

    public void createAssociationNode(Association association) {
        this.association = association;
        this.createUI();
        this.setupDragHandlers();
    }
    
    /**
     * Permet de créer l'UI de l'association (style ovale MCD)
     */
    private void createUI() {
        // Nom de l'association (verbe)
        this.nameLabel = new Label(this.association.getName());
        this.nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        this.nameLabel.setTextFill(Color.web(T_M.getTheme().getTextColor()));
        this.nameLabel.setTextAlignment(TextAlignment.CENTER);
        this.nameLabel.setWrapText(true);
        this.nameLabel.setMaxWidth(120);
        
        this.contentBox.getChildren().clear();
        this.contentBox.getChildren().add(nameLabel);
        
        // Ajouter les attributs de l'association s'il y en a
        if (!this.association.getAttributes().isEmpty()) {
            for (Attribute attr : this.association.getAttributes()) {
                Label attrLabel = new Label(attr.getName());
                attrLabel.setFont(Font.font("System", 11));
                attrLabel.setTextFill(Color.web(T_M.getTheme().getSecondaryTextColor()));
                attrLabel.setTextAlignment(TextAlignment.CENTER);
                this.contentBox.getChildren().add(attrLabel);
            }
        }
        
        // Calculer la taille de l'ellipse en fonction du contenu
        int numLines = 1 + this.association.getAttributes().size();
        double width = 80 + (this.association.getName().length() * 2);
        double height = 40 + (numLines * 15);
        
        // Limites min/max
        width = Math.max(80, Math.min(width, 150));
        height = Math.max(50, Math.min(height, 120));
        
        this.ellipse.setRadiusX(width / 2);
        this.ellipse.setRadiusY(height / 2);
        
        // Taille du StackPane
        this.root.setMinSize(width, height);
        this.root.setPrefSize(width, height);
        this.root.setMaxSize(width, height);
        
        this.updateStyle();
    }
    
    /**
     * Met à jour le style de l'association
     */
    public void updateStyle() {
        // Style ovale pour association MCD
        this.ellipse.setFill(Color.web(T_M.getTheme().getCardColor()));
        this.ellipse.setStroke(Color.web(T_M.getTheme().getBorderColor()));
        this.ellipse.setStrokeWidth(2);
        
        // Effet d'ombre
        this.root.setStyle(
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 1);"
        );
        
        // Mettre à jour les couleurs des labels
        this.nameLabel.setTextFill(Color.web(T_M.getTheme().getTextColor()));
        
        for (int i = 1; i < this.contentBox.getChildren().size(); i++) {
            if (this.contentBox.getChildren().get(i) instanceof Label label) {
                label.setTextFill(Color.web(T_M.getTheme().getSecondaryTextColor()));
            }
        }
    }
    
    /**
     * Met en place la logique de drag
     */
    private void setupDragHandlers() {
        this.root.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (this.onSelect != null) {
                    this.onSelect.accept(this, e);
                }
                this.root.toFront();
                e.consume();
            }
        });
        
        this.root.setOnMouseDragged(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (this.onDrag != null) {
                    this.onDrag.accept(this, e);
                }
                e.consume();
            }
        });

        this.root.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (this.onDragEnd != null) this.onDragEnd.accept(this, e);
                e.consume();
            }
        });
    }

    /**
     * Modifie le style de l'association en fonction de la sélection
     */
    public void setSelected(boolean selected) {
        if (selected) {
            this.ellipse.setStroke(Color.web(T_M.getTheme().getSelectionBorderColor()));
            this.ellipse.setStrokeWidth(3);
        } else {
            this.updateStyle();
        }
    }

    /**
     * Vérifie si un point est contenu dans l'association
     */
    public boolean contains(Point2D pInParent) {
        return this.root.getBoundsInParent().contains(pInParent);
    }

    public void setOnSelect(BiConsumer<AssociationController, MouseEvent> onSelect) {
        this.onSelect = onSelect;
    }
    
    public void setOnDrag(BiConsumer<AssociationController, MouseEvent> onDrag) {
        this.onDrag = onDrag;
    }
    
    public void setOnDragEnd(BiConsumer<AssociationController, MouseEvent> onDragEnd) {
        this.onDragEnd = onDragEnd;
    }
    
    public Association getAssociation() { return association; }
    public StackPane getRoot() { return this.root; }
    
    /**
     * Retourne le centre de l'association pour les connexions
     */
    public Point2D getCenter() {
        return new Point2D(
            this.root.getLayoutX() + this.root.getWidth() / 2,
            this.root.getLayoutY() + this.root.getHeight() / 2
        );
    }
}