package com.dbeditor.controller.node;

import com.dbeditor.model.Entity;
import com.dbeditor.util.ThemeManager;

public class EntityController extends NodeController {

    private static final ThemeManager T_M = ThemeManager.getInstance();

    private Entity entity;

    /**
     * Permet de mettre en place le visuel de la table.<br>
     * Met aussi en en place l'ui et le drag
     */
    public void createEntityController(Entity entity) {
        this.entity = entity;
        super.createNodeController(entity);
    }
    
    @Override
    protected void updateStyleType() {
        super.pane.setStyle(
            "-fx-background-color: " + T_M.getTheme().getCardColor() + "; " +
            "-fx-border-color: " + T_M.getTheme().getBorderColor() + "; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);"
        );
        super.ellipse.setStyle("-fx-background-color: transparent");
        super.hName.setStyle(
            "-fx-background-color: " + T_M.getTheme().getHeaderColor() + "; " +
            "-fx-translate-x: 1px; -fx-translate-y: 1px;"
        );
    }

    public Entity getEntity() { return this.entity; }
}
