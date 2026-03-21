package com.dbeditor.controller.view;

import java.io.IOException;

import com.dbeditor.MainApp;
import com.dbeditor.controller.ViewType;
import com.dbeditor.model.DatabaseSchema;

import javafx.scene.control.ToolBar;
import javafx.scene.layout.Pane;

public class SdfController extends View {

    @Override
    public ViewType getViewType() {
        return ViewType.SDF;
    }

    @Override
    public void initialization(ToolBar toolbar) {
        // TODO
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

    @Override
    public Pane getRoot() {
        // TODO
        return null;
    }

    @Override
    public void updateStyle() {
        // TODO
    }
    
}
