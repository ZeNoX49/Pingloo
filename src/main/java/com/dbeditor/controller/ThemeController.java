package com.dbeditor.controller;

import java.io.IOException;

import com.dbeditor.model.Column;
import com.dbeditor.model.Table;
import com.dbeditor.util.theme.PersoTheme;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class ThemeController {

    @FXML private AnchorPane pane;
    @FXML private Label labelBackgroundColor, labelBorderColor, labelCardColor, labelHeaderColor, labelSecondaryTextColor, labelSelectionBorderColor, labelTextColor, labelToolbarBorderColor, labelToolbarColor;
    @FXML private ColorPicker cpBackgroundColor, cpBorderColor, cpCardColor, cpHeaderColor, cpSecondaryTextColor, cpSelectionBorderColor, cpTextColor, cpToolbarBorderColor, cpToolbarColor;
    @FXML private ToolBar toolBar;
    @FXML private StackPane spTable;

    private PersoTheme perso;
    private AnchorPane tablePane;

    @FXML
    private void initialize() throws IOException {
        this.perso = new PersoTheme();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/table.fxml"));
        this.tablePane = loader.load();
        TableController nodeController = loader.getController();
        nodeController.createTableNode(this.createExampleTable());
        this.spTable.getChildren().add(this.tablePane);
        
        this.pane.setStyle("-fx-background-color: " + perso.getBackgroundColor() + ";");

        this.cpBackgroundColor.setValue(Color.web(this.perso.getBackgroundColor()));
        this.cpCardColor.setValue(Color.web(this.perso.getCardColor()));
        this.cpBorderColor.setValue(Color.web(this.perso.getBorderColor()));
        this.cpSelectionBorderColor.setValue(Color.web(this.perso.getSelectionBorderColor()));
        this.cpHeaderColor.setValue(Color.web(this.perso.getHeaderColor()));
        this.cpTextColor.setValue(Color.web(this.perso.getTextColor()));
        this.cpSecondaryTextColor.setValue(Color.web(this.perso.getSecondaryTextColor()));
        this.cpToolbarColor.setValue(Color.web(this.perso.getToolbarColor()));
        this.cpToolbarBorderColor.setValue(Color.web(this.perso.getToolbarBorderColor()));

        this.cpBackgroundColor.valueProperty().addListener((_, _, newColor) -> {
            this.pane.setStyle("-fx-background-color: " + this.colorToHex(newColor) + ";");
        });
    }

    private String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private Table createExampleTable() {
        Table table = new Table("EXAMPLE");
        table.addColumn(new Column("ex_int", "INT(32)"));
        table.addColumn(new Column("ex_varchar", "VARCHAR(100)"));
        return table;
    }

}