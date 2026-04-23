package com.dbeditor.controller.node;

import com.dbeditor.model.Association;
import com.dbeditor.util.ThemeManager;

public class AssociationController extends NodeController {

    private static final ThemeManager T_M = ThemeManager.getInstance();

    private Association association;

    /**
     * Permet de mettre en place le visuel de la table.<br>
     * Met aussi en en place l'ui et le drag
     */
    public void createAssociationController(Association association) {
        this.association = association;
        super.createNodeController(association);
    }
    
    @Override
    protected void updateStyleType() {
        super.pane.setStyle("-fx-background-color: transparent");
        super.ellipse.setStyle(
            "-fx-background-color: " + T_M.getTheme().getCardColor() + "; " +
            "-fx-border-color: " + T_M.getTheme().getBorderColor() + "; " +
            "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);"
        );
        super.hName.setStyle(
            "-fx-translate-x: 1px; -fx-translate-y: 1px;"
        );
    }

    public Association getAssociation() { return this.association; }
}
