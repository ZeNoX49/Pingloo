package com.dbeditor.controller.node;

import com.dbeditor.model.ConceptualNode;
import com.dbeditor.util.ThemeManager;

public class TableController extends NodeController {

    private static final ThemeManager T_M = ThemeManager.getInstance();

    // private Table table;

    /**
     * Permet de mettre en place le visuel de la table.<br>
     * Met aussi en en place l'ui et le drag
     */
    public void createTableController(ConceptualNode table) {
        // this.table = newTable(table);
        super.createNodeController(table);
    }
    
    @Override
    protected void updateStyleType() {
        super.pane.setStyle(
            "-fx-background-color: " + T_M.getTheme().getCardColor() + "; " +
            "-fx-border-color: " + T_M.getTheme().getBorderColor() + "; " +
            "-fx-border-radius: 8; -fx-background-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);"
        );
        super.ellipse.setStyle("-fx-background-color: transparent");
        super.hName.setStyle(
            "-fx-background-color: " + T_M.getTheme().getHeaderColor() + "; " +
            "-fx-background-radius: 8 8 0 0; " + 
            "-fx-translate-x: 1px; -fx-translate-y: 1px;"
        );
    }

    // public Table getTable() { return this.table; }
}
