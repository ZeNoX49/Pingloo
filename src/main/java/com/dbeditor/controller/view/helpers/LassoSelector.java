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
 * Lasso rectangulaire : attache des handlers sur le 'viewportPane' et ajoute une Rectangle dans 'content'.
 * tableNodes : la liste des TableController à tester.
 */
public class LassoSelector {
    private final Pane viewportPane;   // reçoit les events (scene coords)
    private final Group content;   // parent local pour le rectangle
    private final SelectionModel selectionModel;
    private final Rectangle rect;
    private Point2D startLocal;
    private final List<TableController> tableNodes;

    public LassoSelector(Pane viewportPane, Group content, List<TableController> tableNodes, SelectionModel selectionModel) {
        this.viewportPane = viewportPane;
        this.content = content;
        this.selectionModel = selectionModel;
        this.tableNodes = tableNodes;

        this.rect = new Rectangle();
        this.rect.setManaged(false);
        this.rect.setMouseTransparent(true);
        this.rect.setStyle("-fx-fill: #4A90E235; -fx-stroke: #4A90E2; -fx-stroke-width: 1.5;");
        this.rect.setVisible(false);
        this.content.getChildren().add(this.rect);
    }

    /**
     * Permet de setup les événements :
     * - onPressed
     * - onDragged
     * - onReleased
     */
    public void setupEvents() {
        this.viewportPane.setOnMousePressed(this::onPressed);
        this.viewportPane.setOnMouseDragged(this::onDragged);
        this.viewportPane.setOnMouseReleased(this::onReleased);
    }

    private void onPressed(MouseEvent e) {
        // si pas un clic gauche
        if (e.getButton() != MouseButton.PRIMARY) return;

        // si le clic n'est pas dans cette vue (le pane parent)
        if (e.getTarget() != viewportPane) return;

        this.selectionModel.clear();
        startLocal = this.content.sceneToLocal(e.getSceneX(), e.getSceneY());
        this.rect.setX(startLocal.getX());
        this.rect.setY(startLocal.getY());
        this.rect.setWidth(0);
        this.rect.setHeight(0);
        this.rect.setVisible(true);

        e.consume();
    }

    private void onDragged(MouseEvent e) {
        // sécurité
        if (!this.rect.isVisible()) return;

        Point2D cur = this.content.sceneToLocal(e.getSceneX(), e.getSceneY());
        double x = Math.min(startLocal.getX(), cur.getX());
        double y = Math.min(startLocal.getY(), cur.getY());
        double w = Math.abs(cur.getX() - startLocal.getX());
        double h = Math.abs(cur.getY() - startLocal.getY());
        this.rect.setX(x);
        this.rect.setY(y);
        this.rect.setWidth(w);
        this.rect.setHeight(h);

        // sélectionner les tables qui intersectent
        Bounds rectBounds = this.rect.getBoundsInParent();
        this.selectionModel.clear();
        for (TableController tc : this.tableNodes) {
            if (tc.getRoot().getBoundsInParent().intersects(rectBounds)) {
                this.selectionModel.select(tc);
            }
        }

        e.consume();
    }

    private void onReleased(MouseEvent e) {
        if (this.rect.isVisible()) {
            this.rect.setVisible(false);
            e.consume();
        }
    }

    public Rectangle getRect() { return this.rect; }
}
