package com.dbeditor.util.theme;

import java.util.Map;

import javafx.scene.paint.Color;

public class PersoTheme implements Theme {
    private static String backgroundColor = "#333333";
    private static String cardColor = "#333333";
    private static String borderColor = "#333333";
    private static String selectionBorderColor = "#333333";
    private static String headerColor = "#333333";
    private static String textColor = "#333333";
    private static String secondaryTextColor = "#333333";
    private static String toolbarColor = "#333333";
    private static String toolbarBorderColor = "#333333";
    
    public static void loadColor(Map<String, String> theme) {
        backgroundColor = theme.get("background_color");
        cardColor = theme.get("card_color");
        borderColor = theme.get("border_color");
        selectionBorderColor = theme.get("selection_border_color");
        headerColor = theme.get("header_color");
        textColor = theme.get("text_color");
        secondaryTextColor = theme.get("secondary_text_color");
        toolbarColor = theme.get("toolbar_color");
        toolbarBorderColor = theme.get("toolbar_border_color");
    }

    public void setBackgroundColor(String bc) { backgroundColor = bc; }
    public void setCardColor(String cc) { cardColor = cc; }
    public void setBorderColor(String bc) { borderColor = bc; }
    public void setSelectionBorderColor(String sbc) { selectionBorderColor = sbc; }
    public void setHeaderColor(String hc) { headerColor = hc; }
    public void setTextColor(String tc) { textColor = tc; }
    public void setSecondaryTextColor(String stc) { secondaryTextColor = stc; }
    public void setToolbarColor(String tbc) { toolbarColor = tbc; }
    public void setToolbarBorderColor(String tbdc) { toolbarBorderColor = tbdc; }

    /* ==================================================================================================== */

    @Override
    public int getId() { return 3; }

    @Override
    public String getBackgroundColor() { return backgroundColor; }
    
    @Override
    public String getCardColor() { return cardColor; }
    
    @Override
    public String getBorderColor() { return borderColor; }

    @Override
    public String getSelectionBorderColor() { return selectionBorderColor; }
    
    @Override
    public String getHeaderColor() { return headerColor; }
    
    @Override
    public String getTextColor() { return textColor; }
    
    @Override
    public String getSecondaryTextColor() { return secondaryTextColor; }
    
    @Override
    public String getToolbarColor() { return toolbarColor; }
    
    @Override
    public String getToolbarBorderColor() { return toolbarBorderColor; }
}