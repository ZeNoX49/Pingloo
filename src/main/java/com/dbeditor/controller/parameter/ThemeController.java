package com.dbeditor.controller.parameter;

import com.dbeditor.manager.Singleton;
import com.dbeditor.manager.ThemeManager;
import com.dbeditor.manager.ThemeManager.ThemeType;
import com.dbeditor.theme.PersoTheme;
import com.dbeditor.theme.Theme;

import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

// TODO: pouvoir copier le theme light ou dark
public class ThemeController {
    
    private static final ThemeManager T_M = Singleton.getInstance(ThemeManager.class);
    
    @FXML private AnchorPane pane;
    @FXML private Label labelBackgroundColor, labelBorderColor, labelCardColor, labelHeaderColor, labelSecondaryTextColor, labelSelectionBorderColor, labelTextColor, labelToolbarBorderColor, labelToolbarColor, labelLassoColor, labelLassoBorderColor;
    @FXML private ColorPicker cpBackgroundColor, cpBorderColor, cpCardColor, cpHeaderColor, cpSecondaryTextColor, cpSelectionBorderColor, cpTextColor, cpToolbarBorderColor, cpToolbarColor, cpLassoColor, cpLassoBorderColor;
    @FXML private ToolBar toolBar;
    @FXML private Rectangle lassoRect;

    @FXML private AnchorPane tablePane, tablePaneSel;
    @FXML private HBox hName, hNameSel;
    @FXML private Label name, l1, l2, sl1, sl2, nameSel, l1Sel, sl1Sel, l2Sel, sl2Sel;

    private PersoTheme perso;

    @FXML
    private void initialize() {
        this.perso = (PersoTheme) T_M.getTheme(ThemeType.PERSO);

        this.hName.setStyle(this.hName.getStyle() + "-fx-translate-x: 1px; -fx-translate-y: 1px;");
        this.hNameSel.setStyle(this.hNameSel.getStyle() + "-fx-translate-x: 1px; -fx-translate-y: 1px;");
        this.lassoRect.setStyle(this.lassoRect.getStyle() + "-fx-stroke-width: 1.5;");

        this.createListener();

        this.loadTheme(this.perso);
    }

    private void createListener() {
        // background color
        this.cpBackgroundColor.valueProperty().addListener((observable, oldColor, newColor) -> {
            String hex = ThemeController.colorToHex(newColor);
            this.perso.setBackgroundColor(hex);
            this.pane.setStyle("-fx-background-color: " + hex + ";");
        });

        // card color
        this.cpCardColor.valueProperty().addListener((observable, oldColor, newColor) -> {
            String hex = ThemeController.colorToHex(newColor);
            this.perso.setCardColor(hex);
            this.tablePane.setStyle(
                    this.tablePane.getStyle()
                    + "-fx-background-color: " + hex + ";"
            );
            this.tablePaneSel.setStyle(
                    this.tablePaneSel.getStyle()
                    + "-fx-background-color: " + hex + ";"
            );
        });

        // border color
        this.cpBorderColor.valueProperty().addListener((observable, oldColor, newColor) -> {
            String hex = ThemeController.colorToHex(newColor);
            this.perso.setBorderColor(hex);
            this.tablePane.setStyle(
                    this.tablePane.getStyle()
                    + "-fx-border-color: " + hex + ";"
            );
        });

        // selection border color
        this.cpSelectionBorderColor.valueProperty().addListener((observable, oldColor, newColor) -> {
            String hex = ThemeController.colorToHex(newColor);
            this.perso.setSelectionBorderColor(hex);
            this.tablePaneSel.setStyle(
                    this.tablePaneSel.getStyle()
                    + "-fx-border-color: " + hex + ";"
            );
        });

        // header color
        this.cpHeaderColor.valueProperty().addListener((observable, oldColor, newColor) -> {
            String hex = ThemeController.colorToHex(newColor);
            this.perso.setHeaderColor(hex);
            this.hName.setStyle(
                    this.hName.getStyle()
                    + "-fx-background-color: " + hex + ";"
            );
            this.hNameSel.setStyle(
                    this.hNameSel.getStyle()
                    + "-fx-background-color: " + hex + ";"
            );
        });

        // text color
        this.cpTextColor.valueProperty().addListener((observable, oldColor, newColor) -> {
            String hex = ThemeController.colorToHex(newColor);
            this.perso.setTextColor(hex);

            Label[] labels = new Label[]{this.labelBackgroundColor, this.labelBorderColor, this.labelCardColor, this.labelHeaderColor, this.labelSecondaryTextColor, this.labelSelectionBorderColor, this.labelTextColor, this.labelToolbarBorderColor, this.labelToolbarColor, this.labelLassoColor, this.labelLassoBorderColor, this.name, this.l1, this.l2, this.nameSel, this.l1Sel, this.l2Sel};
            for (Label l : labels) {
                l.setStyle("-fx-text-fill: " + hex + ";");
            }
        });

        // secondary text color
        this.cpSecondaryTextColor.valueProperty().addListener((observable, oldColor, newColor) -> {
            String hex = ThemeController.colorToHex(newColor);
            this.perso.setSecondaryTextColor(hex);
            this.sl1.setStyle("-fx-text-fill: " + hex + ";");
            this.sl2.setStyle("-fx-text-fill: " + hex + ";");
            this.sl1Sel.setStyle("-fx-text-fill: " + hex + ";");
            this.sl2Sel.setStyle("-fx-text-fill: " + hex + ";");
        });

        // toolbar color
        this.cpToolbarColor.valueProperty().addListener((observable, oldColor, newColor) -> {
            String hex = ThemeController.colorToHex(newColor);
            this.perso.setToolbarColor(hex);
            this.toolBar.setStyle(
                    this.toolBar.getStyle()
                    + "-fx-background-color: " + hex + ";"
            );
        });

        // toolbar border color
        this.cpToolbarBorderColor.valueProperty().addListener((observable, oldColor, newColor) -> {
            String hex = ThemeController.colorToHex(newColor);
            this.perso.setToolbarBorderColor(hex);
            this.toolBar.setStyle(
                    this.toolBar.getStyle()
                    + "-fx-border-color: " + hex + ";"
            );
        });

        this.cpLassoColor.valueProperty().addListener((observable, oldColor, newColor) -> {
            String hex = ThemeController.colorToHex(newColor);
            // TODO: this.perso.setLassoColor(hex);
            this.lassoRect.setStyle(
                    this.lassoRect.getStyle()
                    + "-fx-fill: " + hex + ";"
            );
        });
        
        this.cpLassoBorderColor.valueProperty().addListener((observable, oldColor, newColor) -> {
            String hex = ThemeController.colorToHex(newColor);
            // TODO: this.perso.setLassoBorderColor(hex);
            this.lassoRect.setStyle(
                    this.lassoRect.getStyle()
                    + "-fx-stroke: " + hex + ";"
            );
        });
    }

    private static String colorToHex(Color color) {
        return String.format("#%02X%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                (int) (color.getOpacity() * 255)
        );
    }

    @FXML
    private void copyLightTheme() {
        this.loadTheme(T_M.getTheme(ThemeType.LIGHT));
    }

    @FXML
    private void copyDarkTheme() {
        this.loadTheme(T_M.getTheme(ThemeType.DARK));
    }

    private void loadTheme(Theme theme) {
        this.cpBackgroundColor.setValue(Color.web(theme.getBackgroundColor()));
        this.cpCardColor.setValue(Color.web(theme.getCardColor()));
        this.cpBorderColor.setValue(Color.web(theme.getBorderColor()));
        this.cpSelectionBorderColor.setValue(Color.web(theme.getSelectionBorderColor()));
        this.cpHeaderColor.setValue(Color.web(theme.getHeaderColor()));
        this.cpTextColor.setValue(Color.web(theme.getTextColor()));
        this.cpSecondaryTextColor.setValue(Color.web(theme.getSecondaryTextColor()));
        this.cpToolbarColor.setValue(Color.web(theme.getToolbarColor()));
        this.cpToolbarBorderColor.setValue(Color.web(theme.getToolbarBorderColor()));
        this.cpLassoColor.setValue(Color.web("4A90E235"));  // TODO:
        this.cpLassoBorderColor.setValue(Color.web("4A90E2"));  // TODO:
    }

}
