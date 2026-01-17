package com.dbeditor.util;

import com.dbeditor.util.theme.DarkTheme;
import com.dbeditor.util.theme.LightTheme;
import com.dbeditor.util.theme.Theme;

public class ThemeManager {
    private static ThemeManager instance;
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    private ThemeManager() {}

    /* ================================================== */

    private Theme theme = new DarkTheme();

    public Theme getTheme() {
        return this.theme;
    }

    public void changeTheme() {
        if(this.theme instanceof DarkTheme) {
            this.theme = new LightTheme();
        } else {
            this.theme = new DarkTheme();
        }

        // switch (tName) {
        //     case "Dark" :
        //         if (!(this.theme instanceof DarkTheme)) this.theme = new DarkTheme();
        //         break;
        //     case "Light" :
        //         if (!(this.theme instanceof LightTheme)) this.theme = new LightTheme();
        //         break;
        //     case "Perso" :
        //         if (!(this.theme instanceof PersoTheme)) this.theme = new PersoTheme();
        //         break;
        // }
    }
}