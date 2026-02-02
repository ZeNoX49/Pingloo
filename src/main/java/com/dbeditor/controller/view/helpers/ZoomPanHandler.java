package com.dbeditor.controller.view.helpers;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;

/**
 * Handler réutilisable pour zoom + pan.
 * viewport : le Pane qui reçoit les events (ex : pane ou spPane)
 * content : Group (ou Node) qui est zoomé/translaté
 */
public class ZoomPanHandler {
    private final Pane viewport;
    private final Group content;
    private final Scale scale = new Scale(1,1,0,0);
    private double zoomLevel = 1.0;

    private double lastMouseX, lastMouseY;
    private boolean middleMouseDown = false;

    public ZoomPanHandler(Pane viewport, Group content) {
        this.viewport = viewport;
        this.content = content;
        this.content.getTransforms().add(scale);
    }

    public void install() {
        // pan via event filters (capture phase) to avoid child interference
        viewport.addEventFilter(MouseEvent.MOUSE_PRESSED, this::onPressed);
        viewport.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::onDragged);
        viewport.addEventFilter(MouseEvent.MOUSE_RELEASED, this::onReleased);

        // scroll zoom
        viewport.setOnScroll(e -> {
            e.consume();
            double deltaFactor = e.getDeltaY() > 0 ? 1.1 : 0.9;
            Point2D mouseScene = new Point2D(e.getSceneX(), e.getSceneY());
            Point2D mouseLocal = content.sceneToLocal(mouseScene);

            this.zoomLevel = Math.max(0.05, Math.min(this.zoomLevel * deltaFactor, 6.0));
            scale.setX(this.zoomLevel);
            scale.setY(this.zoomLevel);

            Point2D newMouseScene = content.localToScene(mouseLocal);
            double dx = newMouseScene.getX() - mouseScene.getX();
            double dy = newMouseScene.getY() - mouseScene.getY();

            content.setTranslateX(content.getTranslateX() - dx);
            content.setTranslateY(content.getTranslateY() - dy);
        });
    }

    private void onPressed(MouseEvent e) {
        if (e.getButton() == MouseButton.MIDDLE) {
            middleMouseDown = true;
            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();
            viewport.setCursor(Cursor.CLOSED_HAND);
            e.consume();
        }
    }

    private void onDragged(MouseEvent e) {
        if (middleMouseDown && e.isMiddleButtonDown()) {
            double dx = e.getSceneX() - lastMouseX;
            double dy = e.getSceneY() - lastMouseY;
            content.setTranslateX(content.getTranslateX() + dx);
            content.setTranslateY(content.getTranslateY() + dy);
            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();
            e.consume();
        }
    }

    private void onReleased(MouseEvent e) {
        if (middleMouseDown && e.getButton() == MouseButton.MIDDLE) {
            middleMouseDown = false;
            viewport.setCursor(Cursor.DEFAULT);
            e.consume();
        }
    }

    public double getZoomLevel() { return zoomLevel; }
    public Scale getScale() { return scale; }
}
