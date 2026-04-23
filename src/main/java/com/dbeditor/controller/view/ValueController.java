package com.dbeditor.controller.view;

import com.dbeditor.controller.ViewType;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Pane;

public class ValueController extends GridView {

    @Override
    public ViewType getViewType() {
        return ViewType.VALUE;
    }

    private ComboBox<String> combobox;

    @Override
    public void initialization(ToolBar toolbar) {
        // this.combobox = new ComboBox<>();

        // for(Entity table : MainApp.schema.getTables()) {
        //     this.combobox.getItems().add(table.name);
        // }

        // this.combobox.valueProperty().addListener((obs, oldValue, newValue) -> {
        //     if (newValue.equals(oldValue)) return;

        //     this.openTable(MainApp.schema.tables.get(newValue));
        // });

        // toolbar.getItems().add(this.combobox);
        // // super.initialization(toolbar);
    }

    // private void openTable(Entity table) {
    //     // TODO
    // }

    // // @Override
    // protected void setupTableColumns() {
    //     // TODO
    // }

    @Override
    public void open() {
        // TODO
    }

    @Override
    public void updateStyle() {
        // TODO
    }

    @Override
    public void updateType() {
        // TODO
    }

    @Override
    public Pane getRoot() {
        // TODO
        return null;
    }
}