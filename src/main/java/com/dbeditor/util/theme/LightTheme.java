package com.dbeditor.util.theme;

public class LightTheme implements Theme {
    @Override
    public int getId() { return 1; }

    @Override
    public String getBackgroundColor() { return "#f8fafc"; }
    
    @Override
    public String getCardColor() { return "#ffffff"; }
    
    @Override
    public String getBorderColor() { return "#3b82f6"; }

    @Override
    public String getSelectionBorderColor() { return "#a81fde"; }
    
    @Override
    public String getHeaderColor() { return "#3b82f6"; }
    
    @Override
    public String getTextColor() { return "#1e293b"; }
    
    @Override
    public String getSecondaryTextColor() { return "#64748b"; }
    
    @Override
    public String getToolbarColor() { return "#ffffff"; }
    
    @Override
    public String getToolbarBorderColor() { return "#e2e8f0"; }
}