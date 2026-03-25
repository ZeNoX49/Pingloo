package com.dbeditor.controller.view;

import com.dbeditor.util.ThemeManager;

import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public abstract class GridView extends View {
    private static final ThemeManager T_M = ThemeManager.getInstance();

    private TableView tableView;

    @Override
    public void initialization(ToolBar toolbar) {
        this.tableView = new TableView();

        this.updateStyle();
    }

    @Override
    public void updateStyle() {
        // TODO
    }

    public TableView getTableView() {
        return this.tableView;
    }

    @Override
    public Pane getRoot() {
        return null;
        // TODO: return this.tableView;
    }
}
