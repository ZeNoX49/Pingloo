package com.dbeditor.controller.view;

import java.io.IOException;

import com.dbeditor.MainApp;
import com.dbeditor.controller.ViewType;
import com.dbeditor.model.DatabaseSchema;

import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.GridPane;

public class DdController extends GridView {

    @Override
    public ViewType getViewType() {
        return ViewType.DD;
    }

    @Override
    public void initialization(ToolBar toolbar) {
        super.initialization(toolbar);

        GridPane gridPane = (GridPane) this.getRoot();
        gridPane.addColumn(0, new Label("Nom"));
        gridPane.addColumn(1, new Label("Description"));
        gridPane.addColumn(2, new Label("Nature"));
        gridPane.addColumn(3, new Label("Type"));

        this.updateStyle();
    }

    @Override
    public void open(DatabaseSchema dbS) throws IOException {
        // TODO
    }

    @Override
    public DatabaseSchema onSyncGoing(View view) {
        // TODO
        return MainApp.getSchema();
    }

    @Override
    public void onSyncComing(DatabaseSchema dbS) {
        // TODO
    }
    
}