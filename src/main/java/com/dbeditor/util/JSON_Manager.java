package com.dbeditor.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JSON_Manager {
    private static JSON_Manager instance;
    public static JSON_Manager getInstance() {
        if (instance == null) {
            instance = new JSON_Manager();
        }
        return instance;
    }

    private JSON_Manager() {}

    /* ================================================== */

    private ThemeManager T_M = ThemeManager.getInstance();
    private FileManager F_M = FileManager.getInstance();

    private final static Path PATH_TO_FILE_USER_DATA = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "userData.json");

    /**
     * Charge toutes les préférences de l'utilisateur au lancement de l'application.
     * Si le fichier n'existe il est créé
     */
    public void load() throws IOException {
        File file = PATH_TO_FILE_USER_DATA.toFile();

        if (!file.exists()) {
            this.createUserData();
            file = PATH_TO_FILE_USER_DATA.toFile();
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        Map<String, Object> root = mapper.readValue(
            file, Map.class
        );

        F_M.setLastUsedDirectory((String) root.get("last_used_directory"));
        T_M.changeTheme((int) root.get("id_theme"));

        Map<String, String> theme = (Map<String, String>) root.get("perso_theme");
        T_M.getPersoTheme().loadColor(theme);
    }

    /**
     * Sauvegarder toutes les préférences de l'utilisateur au fermement de l'application
     */
    public void save() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Map<String, Object> root = new HashMap<>();
        root.put("last_used_directory", F_M.getLastUserDirectory());
        root.put("id_theme", T_M.getThemeId());

        Map<String, String> themes = new HashMap<>();
        themes.put("background_color", T_M.getPersoTheme().getBackgroundColor());
        themes.put("card_color", T_M.getPersoTheme().getCardColor());
        themes.put("border_color", T_M.getPersoTheme().getBorderColor());
        themes.put("selection_border_color", T_M.getPersoTheme().getSelectionBorderColor());
        themes.put("header_color", T_M.getPersoTheme().getHeaderColor());
        themes.put("text_color", T_M.getPersoTheme().getTextColor());
        themes.put("secondary_text_color", T_M.getPersoTheme().getSecondaryTextColor());
        themes.put("toolbar_color", T_M.getPersoTheme().getToolbarColor());
        themes.put("toolbar_border_color", T_M.getPersoTheme().getToolbarBorderColor());
        root.put("perso_theme", themes);

        try {
            mapper.writeValue(PATH_TO_FILE_USER_DATA.toFile(), root);
            System.out.println("Sauvegarde JSON réussie vers " + PATH_TO_FILE_USER_DATA);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier JSON : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Permet de créer le fichier s'il n'existe pas
     */
    private void createUserData() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Map<String, Object> root = new HashMap<>();
        root.put("last_used_directory", System.getProperty("user.dir"));
        root.put("id_theme", 2);

        Map<String, String> themes = new HashMap<>();
        themes.put("background_color", "#333333");
        themes.put("card_color", "#333333");
        themes.put("border_color", "#333333");
        themes.put("selection_border_color", "#333333");
        themes.put("header_color", "#333333");
        themes.put("text_color", "#333333");
        themes.put("secondary_text_color", "#333333");
        themes.put("toolbar_color", "#333333");
        themes.put("toolbar_border_color", "#333333");
        root.put("perso_theme", themes);

        try {
            mapper.writeValue(PATH_TO_FILE_USER_DATA.toFile(), root);
            System.out.println("Sauvegarde JSON réussie vers " + PATH_TO_FILE_USER_DATA);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier JSON : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}