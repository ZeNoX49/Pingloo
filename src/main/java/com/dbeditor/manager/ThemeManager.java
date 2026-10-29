package com.dbeditor.manager;

import java.util.HashMap;
import java.util.Map;

import com.dbeditor.theme.DarkTheme;
import com.dbeditor.theme.LightTheme;
import com.dbeditor.theme.PersoTheme;
import com.dbeditor.theme.Theme;

public class ThemeManager extends Singleton<ThemeManager> {

    public static enum ThemeType {
        LIGHT, DARK, PERSO;
    }

    private final Map<ThemeType, Theme> themes;
    private ThemeType themeType;
    private Theme theme;

    protected ThemeManager() {
        this.themes = new HashMap<>();
        this.themes.put(ThemeType.LIGHT, new LightTheme());
        this.themes.put(ThemeType.DARK, new DarkTheme());
        this.themes.put(ThemeType.PERSO, new PersoTheme());

        this.changeTheme(ThemeType.DARK);
    }

    public Theme getTheme() {
        return this.theme;
    }

    public final void changeTheme(ThemeType themeType) {
        this.themeType = themeType;
        this.theme = this.themes.get(themeType);
    }

    public PersoTheme getPersoTheme() {
        return (PersoTheme) this.themes.get(ThemeType.PERSO);
    }

    protected ThemeType getThemeType() {
        return this.themeType;
    }
}
