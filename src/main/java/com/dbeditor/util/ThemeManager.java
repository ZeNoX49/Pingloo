package com.dbeditor.util;

import java.util.HashMap;
import java.util.Map;

import com.dbeditor.util.theme.DarkTheme;
import com.dbeditor.util.theme.LightTheme;
import com.dbeditor.util.theme.PersoTheme;
import com.dbeditor.util.theme.Theme;

public class ThemeManager {
    private static ThemeManager instance;
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    private ThemeManager() {
        this.themes = new HashMap<>();
        this.themes.put(1, new LightTheme());
        this.themes.put(2, new DarkTheme());
        this.themes.put(3, new PersoTheme());
    }

    /* ================================================== */

    private int id;
    private Map<Integer, Theme> themes;
    private Theme theme = new DarkTheme();

    public Theme getTheme() {
        return this.theme;
    }

    public void changeTheme(int id) {
        this.id = id;
        this.theme = this.themes.get(id);
    }

    public PersoTheme getPersoTheme() {
        return (PersoTheme) this.themes.get(3);
    }

    public int getThemeId() { return this.id; }
}