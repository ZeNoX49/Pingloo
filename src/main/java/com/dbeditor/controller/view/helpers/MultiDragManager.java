package com.dbeditor.controller.view.helpers;

import java.util.HashMap;
import java.util.Map;

import com.dbeditor.controller.TableController;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;

/**
 * Manager qui permet de déplacer plusieurs TableController ensemble.
 * Il s'appuie sur les callbacks fournis par TableController (setOnDrag, setOnDragEnd).
 */
public class MultiDragManager {
    private final SelectionModel selectionModel;
    private final Map<TableController, Point2D> dragStartPositions = new HashMap<>();
    private Point2D dragStartMouse = null;

    public MultiDragManager(SelectionModel selectionModel) {
        this.selectionModel = selectionModel;
    }

    /**
     * Attache la logique multi-drag à un TableController
     */
    public void attach(TableController tc) {
        // on suppose TableController expose setOnDrag / setOnDragEnd / setOnSelect grâce à SelectionModel
        tc.setOnDrag((t, e) -> handleDrag(t, e));
        tc.setOnDragEnd((t, e) -> handleDragEnd(t, e));
    }

    private void handleDrag(TableController tc, MouseEvent e) {
        // position de la souris dans le parent du node
        Point2D mouseInParent = tc.getRoot().getParent().sceneToLocal(e.getSceneX(), e.getSceneY());

        if (this.dragStartMouse == null) {
            this.dragStartMouse = mouseInParent;
            this.dragStartPositions.clear();

            if (!this.selectionModel.contains(tc)) {
                this.selectionModel.clear();
                this.selectionModel.select(tc);
            }

            for (TableController sel : this.selectionModel.getSelected()) {
                this.dragStartPositions.put(sel, new Point2D(sel.getRoot().getLayoutX(), sel.getRoot().getLayoutY()));
                sel.getRoot().toFront();
            }
        }

        double dx = mouseInParent.getX() - this.dragStartMouse.getX();
        double dy = mouseInParent.getY() - this.dragStartMouse.getY();

        for (Map.Entry<TableController, Point2D> entry : this.dragStartPositions.entrySet()) {
            TableController t = entry.getKey();
            Point2D s = entry.getValue();
            t.getRoot().setLayoutX(s.getX() + dx);
            t.getRoot().setLayoutY(s.getY() + dy);
        }

        e.consume();
    }

    private void handleDragEnd(TableController tc, MouseEvent e) {
        this.dragStartPositions.clear();
        this.dragStartMouse = null;
        e.consume();
    }
}
