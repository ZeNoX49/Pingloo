package com.dbeditor.controller.view.helpers;

import java.util.List;
import javafx.geometry.Point2D;
import javafx.geometry.Bounds;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.Group;
import com.dbeditor.controller.TableController;

/**
 * Lasso rectangular : attache des handlers sur 'viewport' et ajoute une Rectangle dans 'content'.
 * tableNodes : la liste des TableController à tester.
 */
public class LassoSelector {
    private final Pane viewport;   // reçoit les events (scene coords)
    private final Group content;   // parent local pour le rectangle
    private final SelectionModel<TableController> selectionModel;
    private final Rectangle rect;
    private Point2D startLocal;
    private final List<TableController> tableNodes;

    public LassoSelector(Pane viewport, Group content, List<TableController> tableNodes, SelectionModel<TableController> selectionModel) {
        this.viewport = viewport;
        this.content = content;
        this.selectionModel = selectionModel;
        this.tableNodes = tableNodes;

        this.rect = new Rectangle();
        rect.setManaged(false);
        rect.setMouseTransparent(true);
        rect.setStyle("-fx-fill: rgba(74,144,226,0.14); -fx-stroke: #4A90E2; -fx-stroke-width: 1;");
        rect.setVisible(false);
        this.content.getChildren().add(rect);
    }

    public void install() {
        viewport.setOnMousePressed(this::onPressed);
        viewport.setOnMouseDragged(this::onDragged);
        viewport.setOnMouseReleased(this::onReleased);
    }

    private void onPressed(MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) return;

        // only if clicked on background (not on a child) — we check target is viewport itself
        if (e.getTarget() != viewport) return;

        selectionModel.clear();
        startLocal = content.sceneToLocal(e.getSceneX(), e.getSceneY());
        rect.setX(startLocal.getX());
        rect.setY(startLocal.getY());
        rect.setWidth(0);
        rect.setHeight(0);
        rect.setVisible(true);
        e.consume();
    }

    private void onDragged(MouseEvent e) {
        if (!rect.isVisible()) return;
        Point2D cur = content.sceneToLocal(e.getSceneX(), e.getSceneY());
        double x = Math.min(startLocal.getX(), cur.getX());
        double y = Math.min(startLocal.getY(), cur.getY());
        double w = Math.abs(cur.getX() - startLocal.getX());
        double h = Math.abs(cur.getY() - startLocal.getY());
        rect.setX(x); rect.setY(y); rect.setWidth(w); rect.setHeight(h);

        // sélectionner les tables qui intersectent
        Bounds rectBounds = rect.getBoundsInParent();
        selectionModel.clear();
        for (TableController tc : tableNodes) {
            if (tc.getRoot().getBoundsInParent().intersects(rectBounds)) {
                selectionModel.select(tc);
            }
        }
        e.consume();
    }

    private void onReleased(MouseEvent e) {
        if (rect.isVisible()) {
            rect.setVisible(false);
            e.consume();
        }
    }
}
