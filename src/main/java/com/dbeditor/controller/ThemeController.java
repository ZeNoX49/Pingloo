package com.dbeditor.controller;

import java.io.IOException;

import com.dbeditor.model.Column;
import com.dbeditor.model.Table;
import com.dbeditor.util.theme.PersoTheme;

import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class ThemeController {

    @FXML private AnchorPane pane;
    @FXML private Label labelBackgroundColor, labelBorderColor, labelCardColor, labelHeaderColor, labelSecondaryTextColor, labelSelectionBorderColor, labelTextColor, labelToolbarBorderColor, labelToolbarColor;
    @FXML private ColorPicker cpBackgroundColor, cpBorderColor, cpCardColor, cpHeaderColor, cpSecondaryTextColor, cpSelectionBorderColor, cpTextColor, cpToolbarBorderColor, cpToolbarColor;
    @FXML private ToolBar toolBar;
    
    @FXML private AnchorPane tablePane;
    @FXML private HBox hName;
    @FXML private Label name, l1, l2, sl1, sl2;

    private PersoTheme perso;

    @FXML
    private void initialize() throws IOException {
        this.perso = new PersoTheme();
        
        this.createListener();

        this.cpBackgroundColor.setValue(Color.web(this.perso.getBackgroundColor()));
        this.cpCardColor.setValue(Color.web(this.perso.getCardColor()));
        this.cpBorderColor.setValue(Color.web(this.perso.getBorderColor()));
        this.cpSelectionBorderColor.setValue(Color.web(this.perso.getSelectionBorderColor()));
        this.cpHeaderColor.setValue(Color.web(this.perso.getHeaderColor()));
        this.cpTextColor.setValue(Color.web(this.perso.getTextColor()));
        this.cpSecondaryTextColor.setValue(Color.web(this.perso.getSecondaryTextColor()));
        this.cpToolbarColor.setValue(Color.web(this.perso.getToolbarColor()));
        this.cpToolbarBorderColor.setValue(Color.web(this.perso.getToolbarBorderColor()));
    }

    private void createListener() {
        // background color
        this.cpBackgroundColor.valueProperty().addListener((_, _, newColor) -> {
            this.perso.setBackgroundColor(this.colorToHex(newColor));
            this.pane.setStyle("-fx-background-color: " + this.perso.getBackgroundColor() + ";");
        });
        
        // card color
        this.cpCardColor.valueProperty().addListener((_, _, newColor) -> {
            this.perso.setCardColor(this.colorToHex(newColor));
            this.tablePane.setStyle(
                this.tablePane.getStyle() +
                "-fx-background-color: " + this.perso.getCardColor() + ";"
            );
        });
        
        // border color
        this.cpBorderColor.valueProperty().addListener((_, _, newColor) -> {
            this.perso.setBorderColor(this.colorToHex(newColor));
            this.tablePane.setStyle(
                this.tablePane.getStyle() +
                "-fx-border-color: " + this.perso.getBorderColor() + ";"
            );
        });

        // selection border color
        this.cpSelectionBorderColor.valueProperty().addListener((_, _, newColor) -> {
            this.perso.setSelectionBorderColor(this.colorToHex(newColor));
            this.tablePane.setStyle(
                this.tablePane.getStyle() +
                "-fx-border-color: " + this.perso.getBorderSelectionColor() + ";"
            );
        });
        
        // header color
        this.cpHeaderColor.valueProperty().addListener((_, _, newColor) -> {
            this.perso.setHeaderColor(this.colorToHex(newColor));
            this.hName.setStyle(
                this.hName.getStyle() +
                "-fx-background-color: " + this.perso.getHeaderColor() + ";"
            );
        });
        
        // text color
        this.cpTextColor.valueProperty().addListener((_, _, newColor) -> {
            this.perso.setTextColor(this.colorToHex(newColor));
            this.l1.setStyle("-fx-text-fill: " + this.perso.getTextColor() + ";");
            this.l2.setStyle("-fx-text-fill: " + this.perso.getTextColor() + ";");
        });
        
        // secondary text color
        this.cpSecondaryTextColor.valueProperty().addListener((_, _, newColor) -> {
            this.perso.setSecondaryTextColor(this.colorToHex(newColor));
            this.sl1.setStyle("-fx-text-fill: " + this.perso.getSecondaryTextColor() + ";");
            this.sl2.setStyle("-fx-text-fill: " + this.perso.getSecondaryTextColor() + ";");
        });
        
        // toolbar color
        this.cpToolbarColor.valueProperty().addListener((_, _, newColor) -> {
            this.perso.setToolbarColor(this.colorToHex(newColor));
            this.toolBar.setStyle(
                this.toolBar.getStyle() +
                "-fx-background-color: " + this.perso.getToolbarColor() + ";"
            );
        });
        
        // toolbar border color
        this.cpToolbarBorderColor.valueProperty().addListener((_, _, newColor) -> {
            this.perso.setToolbarBorderColor(this.colorToHex(newColor));
            this.toolBar.setStyle(
                this.toolBar.getStyle() +
                "-fx-border-color: " + this.perso.getToolbarBorderColor() + ";"
            );
        });
    }

    private String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}