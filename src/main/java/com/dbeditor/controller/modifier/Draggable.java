package com.dbeditor.controller.modifier;

import java.util.function.Consumer;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public interface Draggable<T> {

    /**
     * Met en place la logique de drag
     */
    public void setupDragHandlers();

    /**
     * Modifie le bord de la table en fonction de la sélection
     */
    public void setSelected(boolean selected);

    /**
     * On regarde si l'élement est déja inclus
     */
    default boolean contains(Point2D pInParent) {
        return this.getRoot().getBoundsInParent().contains(pInParent);
    }

    public void setOnSelect(Consumer<MouseEvent> onSelect);

    public void setOnDrag(Consumer<MouseEvent> onDrag);

    public void setOnDragEnd(Consumer<MouseEvent> onDragEnd);

    public Node getRoot();
}
