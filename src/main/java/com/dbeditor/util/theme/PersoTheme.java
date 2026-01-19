package com.dbeditor.util.theme;

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

    public void setBackgroundColor(String bc) { this.backgroundColor = bc; }
    public void setCardColor(String cc) { this.cardColor = cc; }
    public void setBorderColor(String bc) { this.borderColor = bc; }
    public void setSelectionBorderColor(String sbc) { this.selectionBorderColor = sbc; }
    public void setHeaderColor(String hc) { this.headerColor = hc; }
    public void setTextColor(String tc) { this.textColor = tc; }
    public void setSecondaryTextColor(String stc) { this.secondaryTextColor = stc; }
    public void setToolbarColor(String tbc) { this.toolbarColor = tbc; }
    public void setToolbarBorderColor(String tbdc) { this.toolbarBorderColor = tbdc; }

    /* ==================================================================================================== */

    @Override
    public int getId() { return 3; }

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