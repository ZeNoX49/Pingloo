package com.dbeditor.controller.view;

import com.dbeditor.util.ThemeManager;

import javafx.scene.control.ToolBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public abstract class GridView extends View {
    private static final ThemeManager T_M = ThemeManager.getInstance();

    private GridPane gridPane;

    @Override
    public void initialization(ToolBar toolbar) {
        this.gridPane = new GridPane();

        this.updateStyle();
    }

    @Override
    public void updateStyle() {
        // TODO
    }

    @Override
    public Pane getRoot() {
        return this.gridPane;
    }
}
