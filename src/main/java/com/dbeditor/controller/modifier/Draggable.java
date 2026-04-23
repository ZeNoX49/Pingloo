package com.dbeditor.controller.modifier;

import java.util.function.BiConsumer;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public interface Draggable {
    /**
     * Met en place la logique de drag
     */
    public void setupDragHandlers();

    /**
     * Modifie le bord de la table en fonction de la sélection
     * @param selected
     */
    public void setSelected(boolean selected);

    /**
     * On regarde si l'élement est déja inclus
     * @param pInParent
     * @return
     */
    default boolean contains(Point2D pInParent) {
        return this.getRoot().getBoundsInParent().contains(pInParent);
    }

    public void setOnSelect(BiConsumer<Draggable, MouseEvent> onSelect);
    public void setOnDrag(BiConsumer<Draggable, MouseEvent> onDrag);
    public void setOnDragEnd(BiConsumer<Draggable, MouseEvent> onDragEnd);

    public Node getRoot();
}