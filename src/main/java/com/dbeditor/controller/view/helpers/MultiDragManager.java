package com.dbeditor.controller.view.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.dbeditor.controller.modifier.Draggable;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;

/**
 * Manager qui permet de déplacer plusieurs Node "Drag" ensemble.
 * Il s'appuie sur les callbacks fournis par Node "Drag" (setOnDrag, setOnDragEnd).
 */ 
public class MultiDragManager<D extends Draggable> {
    private final SelectionModel<D> selectionModel;
    private final Map<D, Point2D> dragStartPositions;
    private Point2D dragStartMouse;

    public MultiDragManager(SelectionModel<D> selectionModel) {
        this.selectionModel = selectionModel;
        this.dragStartPositions = new HashMap<>();
    }

    /**
     * Attache la logique multi-drag à un Node "Drag"
     */
    public void attach(D d) {
        d.setOnDrag((t, e) -> this.handleDrag(d, e));
        d.setOnDragEnd((t, e) -> this.handleDragEnd(e));
    }

    private void handleDrag(D d, MouseEvent e) {
        // position de la souris dans le parent du node
        Point2D mouseInParent = d.getRoot().getParent().sceneToLocal(e.getSceneX(), e.getSceneY());

        if (this.dragStartMouse == null) {
            this.dragStartMouse = mouseInParent;
            this.dragStartPositions.clear();

            if (!this.selectionModel.contains(d)) {
                this.selectionModel.clear();
                this.selectionModel.select(d);
            }

            for (D selected : this.selectionModel.getSelected()) {
                this.dragStartPositions.put(selected, new Point2D(selected.getRoot().getLayoutX(), selected.getRoot().getLayoutY()));
                selected.getRoot().toFront();
            }
        }

        double dx = mouseInParent.getX() - this.dragStartMouse.getX();
        double dy = mouseInParent.getY() - this.dragStartMouse.getY();

        for (Entry<D, Point2D> entry : this.dragStartPositions.entrySet()) {
            D node = entry.getKey();
            Point2D s = entry.getValue();
            node.getRoot().setLayoutX(s.getX() + dx);
            node.getRoot().setLayoutY(s.getY() + dy);
        }

        e.consume();
    }

    private void handleDragEnd(MouseEvent e) {
        this.dragStartPositions.clear();
        this.dragStartMouse = null;
        e.consume();
    }
}
