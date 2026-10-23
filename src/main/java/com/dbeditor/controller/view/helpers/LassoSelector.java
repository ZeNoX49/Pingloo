package com.dbeditor.controller.view.helpers;

import java.util.Map;

import com.dbeditor.controller.modifier.Draggable;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

/**
 * Lasso rectangulaire : attache des handlers sur le 'viewportPane' et ajoute
 * une Rectangle dans 'content'. nodes : la liste des Node à tester.
 */
public class LassoSelector<D extends Draggable<?>> {

    private final Pane viewportPane;   // reçoit les events (scene coords)
    private final Group content;   // parent local pour le rectangle
    private final SelectionModel<D> selectionModel;
    public final Rectangle rect;
    private final Map<String, D> nodes;

    private Point2D startLocal;
    private boolean dragging = false;

    public LassoSelector(Pane viewportPane, Group content, Map<String, D> nodes, SelectionModel<D> selectionModel) {
        this.viewportPane = viewportPane;
        this.content = content;
        this.selectionModel = selectionModel;
        this.nodes = nodes;

        // le lasso
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

        // Filet de sécurité : si la souris est relâchée hors du pane,
        // la scène envoie quand même un MOUSE_RELEASED — on le capte ici.
        this.viewportPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                return;
            }

            newScene.setOnMouseReleased(e -> {
                if (this.dragging && e.getButton() == MouseButton.PRIMARY) {
                    this.rect.setVisible(false);
                    this.dragging = false;
                }
            });
        });
    }

    private void onPressed(MouseEvent e) {
        // si pas un clic gauche || si le clic n'est pas dans cette vue (le pane parent)
        if (e.getButton() != MouseButton.PRIMARY || e.getTarget() != this.viewportPane) {
            return;
        }

        this.selectionModel.clear();
        this.startLocal = this.content.sceneToLocal(e.getSceneX(), e.getSceneY());

        this.rect.setX(this.startLocal.getX());
        this.rect.setY(this.startLocal.getY());
        this.rect.setWidth(0);
        this.rect.setHeight(0);
        this.rect.setVisible(true);
        this.rect.toFront();
        this.dragging = true;

        e.consume();
    }

    private void onDragged(MouseEvent e) {
        if (!this.rect.isVisible()) {
            return;
        }

        Point2D cur = this.content.sceneToLocal(e.getSceneX(), e.getSceneY());
        double x = Math.min(this.startLocal.getX(), cur.getX());
        double y = Math.min(this.startLocal.getY(), cur.getY());
        double w = Math.abs(cur.getX() - this.startLocal.getX());
        double h = Math.abs(cur.getY() - this.startLocal.getY());

        this.rect.setX(x);
        this.rect.setY(y);
        this.rect.setWidth(w);
        this.rect.setHeight(h);

        // sélectionner les tables qui intersectent
        Bounds rectBounds = this.rect.getBoundsInParent();
        this.selectionModel.clear();
        for (D node : this.nodes.values()) {
            if (node.getRoot().getBoundsInParent().intersects(rectBounds)) {
                this.selectionModel.select(node);
            }
        }

        e.consume();
    }

    private void onReleased(MouseEvent e) {
        if (this.rect.isVisible()) {
            this.rect.setVisible(false);
            this.dragging = false;
            e.consume();
        }
    }
}
