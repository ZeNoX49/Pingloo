package com.dbeditor.theme;

import java.util.Map;

public class PersoTheme implements Theme {

    private String backgroundColor;
    private String cardColor;
    private String borderColor;
    private String selectionBorderColor;
    private String headerColor;
    private String textColor;
    private String secondaryTextColor;
    private String toolbarColor;
    private String toolbarBorderColor;

    public void loadColor(Map<String, String> theme) {
        this.backgroundColor = theme.get("background_color");
        this.cardColor = theme.get("card_color");
        this.borderColor = theme.get("border_color");
        this.selectionBorderColor = theme.get("selection_border_color");
        this.headerColor = theme.get("header_color");
        this.textColor = theme.get("text_color");
        this.secondaryTextColor = theme.get("secondary_text_color");
        this.toolbarColor = theme.get("toolbar_color");
        this.toolbarBorderColor = theme.get("toolbar_border_color");
    }

    public void setBackgroundColor(String color) {
        this.backgroundColor = color;
    }

    public void setCardColor(String color) {
        this.cardColor = color;
    }

    public void setBorderColor(String color) {
        this.borderColor = color;
    }

    public void setSelectionBorderColor(String color) {
        this.selectionBorderColor = color;
    }

    public void setHeaderColor(String color) {
        this.headerColor = color;
    }

    public void setTextColor(String color) {
        this.textColor = color;
    }

    public void setSecondaryTextColor(String color) {
        this.secondaryTextColor = color;
    }

    public void setToolbarColor(String color) {
        this.toolbarColor = color;
    }

    public void setToolbarBorderColor(String color) {
        this.toolbarBorderColor = color;
    }

    /* ==================================================================================================== */

    @Override
    public String getBackgroundColor() {
        return this.backgroundColor;
    }

    @Override
    public String getCardColor() {
        return this.cardColor;
    }

    @Override
    public String getBorderColor() {
        return this.borderColor;
    }

    @Override
    public String getSelectionBorderColor() {
        return this.selectionBorderColor;
    }

    @Override
    public String getHeaderColor() {
        return this.headerColor;
    }

    @Override
    public String getTextColor() {
        return this.textColor;
    }

    @Override
    public String getSecondaryTextColor() {
        return this.secondaryTextColor;
    }

    @Override
    public String getToolbarColor() {
        return this.toolbarColor;
    }

    @Override
    public String getToolbarBorderColor() {
        return this.toolbarBorderColor;
    }
}
