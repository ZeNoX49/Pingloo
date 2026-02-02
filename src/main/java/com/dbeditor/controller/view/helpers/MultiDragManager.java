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
    private final SelectionModel<TableController> selectionModel;
    private final Map<TableController, Point2D> dragStartPositions = new HashMap<>();
    private Point2D dragStartMouse = null;

    public MultiDragManager(SelectionModel<TableController> selectionModel) {
        this.selectionModel = selectionModel;
    }

    /**
     * Attache la logique multi-drag à un TableController
     */
    public void attach(TableController tc) {
        // on suppose TableController expose setOnDrag / setOnDragEnd / setOnSelect
        tc.setOnDrag((t, e) -> handleDrag(t, e));
        tc.setOnDragEnd((t, e) -> handleDragEnd(t, e));
        // selection handled by Canvas/Mld code using selectionModel
    }

    private void handleDrag(TableController tc, MouseEvent e) {
        // position de la souris dans le parent du node (TableController doit envoyer event avec scene coords)
        Point2D mouseInParent = tc.getRoot().getParent().sceneToLocal(e.getSceneX(), e.getSceneY());

        if (dragStartMouse == null) {
            dragStartMouse = mouseInParent;
            dragStartPositions.clear();

            if (!selectionModel.contains(tc)) {
                selectionModel.clear();
                selectionModel.select(tc);
            }

            for (TableController sel : selectionModel.getSelected()) {
                dragStartPositions.put(sel, new Point2D(sel.getRoot().getLayoutX(), sel.getRoot().getLayoutY()));
                sel.getRoot().toFront();
            }
        }

        double dx = mouseInParent.getX() - dragStartMouse.getX();
        double dy = mouseInParent.getY() - dragStartMouse.getY();

        for (Map.Entry<TableController, Point2D> entry : dragStartPositions.entrySet()) {
            TableController t = entry.getKey();
            Point2D s = entry.getValue();
            t.getRoot().setLayoutX(s.getX() + dx);
            t.getRoot().setLayoutY(s.getY() + dy);
        }

        e.consume();
    }

    private void handleDragEnd(TableController tc, MouseEvent e) {
        dragStartPositions.clear();
        dragStartMouse = null;
        e.consume();
    }
}
