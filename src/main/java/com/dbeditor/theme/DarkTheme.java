package com.dbeditor.theme;

public class DarkTheme implements Theme {
    @Override
    public int getId() { return 2; }
    
    @Override
    public String getBackgroundColor() { return "#1e293b"; }
    
    @Override
    public String getCardColor() { return "#334155"; }
    
    @Override
    public String getBorderColor() { return "#60a5fa"; }
    
    @Override
    public String getSelectionBorderColor() { return "#de981f"; }
    
    @Override
    public String getHeaderColor() { return "#1e40af"; }
    
    @Override
    public String getTextColor() { return "#f1f5f9"; }
    
    @Override
    public String getSecondaryTextColor() { return "#94a3b8"; }
    
    @Override
    public String getToolbarColor() { return "#0f172a"; }
    
    @Override
    public String getToolbarBorderColor() { return "#334155"; }
}
