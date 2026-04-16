package com.dbeditor.controller.view;

import java.io.IOException;

import com.dbeditor.controller.ViewType;
import com.dbeditor.sql.DbType;

import javafx.scene.control.ToolBar;

// TODO
public class DdController extends GridView {

    @Override
    public ViewType getViewType() {
        return ViewType.DD;
    }

    @Override
    public void initialization(ToolBar toolbar) {
        super.initialization(toolbar);

        // TableView tableView = super.getTableView();
        // tableView.add

        // GridPane gridPane = (GridPane) this.getRoot();
        // gridPane.addColumn(0, new Label("Nom"));
        // gridPane.addColumn(1, new Label("Description"));
        // gridPane.addColumn(2, new Label("Nature"));
        // gridPane.addColumn(3, new Label("Type"));

        this.updateStyle();
    }

    @Override
    public void open() throws IOException {
        // TODO
    }

    @Override
    public void updateType(DbType type) {
        // TODO
    }
    
}