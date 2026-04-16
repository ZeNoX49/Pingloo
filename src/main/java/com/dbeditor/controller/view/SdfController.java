package com.dbeditor.controller.view;

import java.io.IOException;

import com.dbeditor.controller.ViewType;
import com.dbeditor.sql.DbType;

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
    public void open() throws IOException {
        // TODO
    }

    @Override
    public void updateType(DbType type) {
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
