package com.dbeditor.controller.view.helpers;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;

/**
 * Handler réutilisable pour zoom + pan.
 * viewportPane : le Pane qui reçoit les events (ex : pane ou spPane)
 * content : Group (ou Node) qui est zoomé/translaté
 */
public class ZoomPanHandler {
    private final Pane viewportPane;
    private final Group content;
    private final Scale scale = new Scale(1, 1, 0, 0);
    private double zoomLevel = 1.0;

    private double lastMouseX, lastMouseY;
    private boolean middleMouseDown = false;

    public ZoomPanHandler(Pane viewportPane, Group content) {
        this.viewportPane = viewportPane;
        this.content = content;
        this.content.getTransforms().add(this.scale);
    }

    /**
     * Permet de setup les événements :
     * - onPressed
     * - onDragged
     * - onReleased
     * - OnScroll
     */
    public void setupEvents(Label zlLabel) {
        // pan via event filters (capture phase) to avoid child interference
        this.viewportPane.addEventFilter(MouseEvent.MOUSE_PRESSED, this::onPressed);
        this.viewportPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::onDragged);
        this.viewportPane.addEventFilter(MouseEvent.MOUSE_RELEASED, this::onReleased);

        zlLabel.setText("%.2f".formatted(this.zoomLevel));

        // scroll zoom
        this.viewportPane.setOnScroll(e -> {
            e.consume();
            
            double deltaFactor = e.getDeltaY() > 0 ? 1.1 : 0.9;
            Point2D mouseScene = new Point2D(e.getSceneX(), e.getSceneY());
            Point2D mouseLocal = content.sceneToLocal(mouseScene);

            this.zoomLevel = Math.max(0.05, Math.min(this.zoomLevel * deltaFactor, 6.0));
            this.scale.setX(this.zoomLevel);
            this.scale.setY(this.zoomLevel);

            Point2D newMouseScene = this.content.localToScene(mouseLocal);
            double dx = newMouseScene.getX() - mouseScene.getX();
            double dy = newMouseScene.getY() - mouseScene.getY();

            this.content.setTranslateX(this.content.getTranslateX() - dx);
            this.content.setTranslateY(this.content.getTranslateY() - dy);

            zlLabel.setText("%.2f".formatted(this.zoomLevel));
        });
    }

    private void onPressed(MouseEvent e) {
        if (e.getButton() != MouseButton.MIDDLE) return;

        this.middleMouseDown = true;
        this.lastMouseX = e.getSceneX();
        this.lastMouseY = e.getSceneY();
        this.viewportPane.setCursor(Cursor.CLOSED_HAND);
        e.consume();
    }

    private void onDragged(MouseEvent e) {
        if (!this.middleMouseDown || !e.isMiddleButtonDown()) return;
        
        double dx = e.getSceneX() - lastMouseX;
        double dy = e.getSceneY() - lastMouseY;
        this.content.setTranslateX(this.content.getTranslateX() + dx);
        this.content.setTranslateY(this.content.getTranslateY() + dy);
        lastMouseX = e.getSceneX();
        lastMouseY = e.getSceneY();
        e.consume();
    }

    private void onReleased(MouseEvent e) {
        if (!this.middleMouseDown || e.getButton() != MouseButton.MIDDLE) return;

        this.middleMouseDown = false;
        this.viewportPane.setCursor(Cursor.DEFAULT);
        e.consume();
    }
}
