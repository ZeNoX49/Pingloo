package com.dbeditor.util.theme;

public class PersoTheme implements Theme {
    private String backgroundColor = "#333333";
    private String cardColor = "#333333";
    private String borderColor = "#333333";
    private String selectionBorderColor = "#333333";
    private String headerColor = "#333333";
    private String textColor = "#333333";
    private String secondaryTextColor = "#333333";
    private String toolbarColor = "#333333";
    private String toolbarBorderColor = "#333333";
    
    public void loadColor() {
        // TODO: ouvrir fichier

        this.backgroundColor = "#333333";
        this.cardColor = "#333333";
        this.borderColor = "#333333";
        this.selectionBorderColor = "#333333";
        this.headerColor = "#333333";
        this.textColor = "#333333";
        this.secondaryTextColor = "#333333";
        this.toolbarColor = "#333333";
        this.toolbarBorderColor = "#333333";
    }

    @Override
    public String getName() { return "Perso"; }

    @Override
    public String getBackgroundColor() { return this.backgroundColor; }
    
    @Override
    public String getCardColor() { return this.cardColor; }
    
    @Override
    public String getBorderColor() { return this.borderColor; }

    @Override
    public String getSelectionBorderColor() { return this.selectionBorderColor; }
    
    @Override
    public String getHeaderColor() { return this.headerColor; }
    
    @Override
    public String getTextColor() { return this.textColor; }
    
    @Override
    public String getSecondaryTextColor() { return this.secondaryTextColor; }
    
    @Override
    public String getToolbarColor() { return this.toolbarColor; }
    
    @Override
    public String getToolbarBorderColor() { return this.toolbarBorderColor; }
}