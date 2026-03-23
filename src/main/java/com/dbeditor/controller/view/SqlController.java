package com.dbeditor.controller.view;

import java.io.IOException;

import com.dbeditor.MainApp;
import com.dbeditor.controller.ViewType;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.sql.DbType;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;


public class SqlController extends TextView {

    @Override
    public ViewType getViewType() {
        return ViewType.SQL;
    }

    private ComboBox<String> combobox;

    @Override
    public void initialization(ToolBar toolbar) {
        this.combobox = new ComboBox<>();
        this.combobox.getItems().setAll(
            DbType.MySql.toString(),
            DbType.MsSql.toString(),
            DbType.PostgreSql.toString()
        );
        toolbar.getItems().add(this.combobox);

        super.initialization(toolbar);
    }

    @Override
    public void updateStyle() {
        // TODO
    }
    
    @Override
    public void open(DatabaseSchema dbS) throws IOException {
        // TODO
    }
    
}