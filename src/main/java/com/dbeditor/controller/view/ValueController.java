package com.dbeditor.controller.view;

import com.dbeditor.MainApp;
import com.dbeditor.controller.ViewType;
import com.dbeditor.model.Table;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;

public class ValueController extends GridView {

    @Override
    public ViewType getViewType() {
        return ViewType.VALUE;
    }

    private ComboBox<String> combobox;

    @Override
    public void initialization(ToolBar toolbar) {
        this.combobox = new ComboBox<>();

        for(Table table : MainApp.schema.getTables()) {
            this.combobox.getItems().add(
                table.name
            );
        }

        toolbar.getItems().add(this.combobox);

        super.initialization(toolbar);
    }

    @Override
    public void updateStyle() {
        // TODO
    }

    @Override
    public void open() {
        // TODO
    }

    @Override
    public void updateType() {
        // TODO
    }
}