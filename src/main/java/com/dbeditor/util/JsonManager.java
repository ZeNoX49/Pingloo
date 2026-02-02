package com.dbeditor.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonManager {
    private static JsonManager instance;
    public static JsonManager getInstance() {
        if (instance == null) {
            instance = new JsonManager();
        }
        return instance;
    }

    private JsonManager() {}

    /* ================================================== */

    private DbManager D_M = DbManager.getInstance();
    private ThemeManager T_M = ThemeManager.getInstance();
    private FileManager F_M = FileManager.getInstance();

    private final static Path PATH_TO_FILE_USER_DATA = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "userPreferences.json");

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

        Map<String, Object> root = mapper.readValue(file, Map.class);

        T_M.changeTheme((int) root.get("id_theme"));
        
        String lud = (String) root.get("last_used_directory");
        if(lud == null || lud.trim().isEmpty()) {
            F_M.setLastUsedDirectory(System.getProperty("user.dir"));
        } else {
            F_M.setLastUsedDirectory(lud);
        }

        T_M.getPersoTheme().loadColor((Map<String, String>) root.get("perso_theme"));

        Map<String, Map<String, Object>> db = (Map<String, Map<String, Object>>) root.get("db");
        Map<String, Object> mysql = db.get("mysql");
        D_M.setMysqlDbData(mysql);
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

        Map<String, Map<String, Object>> db = new HashMap<>();
        Map<String, Object> mysql = new HashMap<>();
        mysql.put("host", "localhost");
        mysql.put("user", "root");
        mysql.put("password", "");
        mysql.put("port", "3306");
        List<Map<String, String>> tables = new ArrayList<>();
        for(String t : D_M.getMysqlDbTables()) {
            Map<String, String> map = new HashMap<>();
            map.put("name", t);
            tables.add(map);
        }
        mysql.put("tables", tables);
        db.put("mysql", mysql);
        root.put("db", db);

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

        Map<String, Map<String, Object>> db = new HashMap<>();
        Map<String, Object> mysql = new HashMap<>();
        mysql.put("host", "localhost");
        mysql.put("user", "root");
        mysql.put("password", "");
        mysql.put("port", "3306");
        mysql.put("tables", new ArrayList<Map<String, String>>());
        db.put("mysql", mysql);
        root.put("db", db);

        try {
            mapper.writeValue(PATH_TO_FILE_USER_DATA.toFile(), root);
            System.out.println("Sauvegarde JSON réussie vers " + PATH_TO_FILE_USER_DATA);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier JSON : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}