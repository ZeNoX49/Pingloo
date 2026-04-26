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
    public void attach(D node) {
        node.setOnDrag((t, e) -> this.handleDrag(node, e));
        node.setOnDragEnd((t, e) -> this.handleDragEnd(e));
    }

    private void handleDrag(D node, MouseEvent e) {
        // position de la souris dans le parent du node
        Point2D mouseInParent = node.getRoot().getParent().sceneToLocal(e.getSceneX(), e.getSceneY());

        if (this.dragStartMouse == null) {
            this.dragStartMouse = mouseInParent;
            this.dragStartPositions.clear();

            if (!this.selectionModel.contains(node)) {
                this.selectionModel.clear();
                this.selectionModel.select(node);
            }

            for (D selected : this.selectionModel.getSelected()) {
                this.dragStartPositions.put(selected, new Point2D(selected.getRoot().getLayoutX(), selected.getRoot().getLayoutY()));
                selected.getRoot().toFront();
            }
        }

        double dx = mouseInParent.getX() - this.dragStartMouse.getX();
        double dy = mouseInParent.getY() - this.dragStartMouse.getY();

        for (Entry<D, Point2D> entry : this.dragStartPositions.entrySet()) {
            D n = entry.getKey();
            Point2D s = entry.getValue();
            n.getRoot().setLayoutX(s.getX() + dx);
            n.getRoot().setLayoutY(s.getY() + dy);
        }

        e.consume();
    }

    private void handleDragEnd(MouseEvent e) {
        this.dragStartPositions.clear();
        this.dragStartMouse = null;
        e.consume();
    }
}
