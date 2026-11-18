package com.dbeditor.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.dbeditor.theme.PersoTheme;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

//
// TODO : ajouté un type de "profil" de les types de bdd et pouvoir activer/désactiver les bdd chosis
// <br>
// "mysql" : [
//     {
//         "name" : "xampp",
//         "host" : "localhost",
//         "user" : "root",
//         "password" : "",
//         "port" : "3306",
//         "databases" : [
//             {
//                 "name" : "ecogestum",
//                 "enabled" : true
//             },
//             {
//                 "name" : "pokemontgcp",
//                 "enabled" : false
//             },
//             {
//                 "name" : "sae4",
//                 "enabled" : true
//             }
//         ]
//     },
//     {
//         ...
//     }
// ]
//
public class JsonManager extends Singleton<JsonManager> {

    // private static final DbManager D_M = Singleton.getInstance(DbManager.class);
    private static final ThemeManager T_M = Singleton.getInstance(ThemeManager.class);
    // private static final FileManager F_M = Singleton.getInstance(FileManager.class);

    private static final Path PATH_TO_FILE_USER_DATA = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "userPreferences.json");

    /**
     * Charge toutes les préférences de l'utilisateur au lancement de
     * l'application. Si le fichier n'existe il est créé
     */
    public void load() {
        File file = JsonManager.PATH_TO_FILE_USER_DATA.toFile();

        if (!file.exists()) {
            this.createUserData();
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        try {
            Map<String, Object> root = mapper.readValue(file, Map.class);

            T_M.changeTheme(ThemeManager.ThemeType.valueOf((String) root.get("theme_type")));

            // String lud = (String) root.get("last_used_directory");
            // if (lud == null || lud.trim().isEmpty()) {
            //     F_M.setLastUsedDirectory(System.getProperty("user.dir"));
            // } else {
            //     F_M.setLastUsedDirectory(lud);
            // }

            PersoTheme pt = (PersoTheme) T_M.getTheme(ThemeManager.ThemeType.PERSO);
            pt.loadColor((Map<String, String>) root.get("perso_theme"));

            // Map<String, Map<String, Object>> db = (Map<String, Map<String, Object>>) root.get("db_type");
            // Map<String, Object> mysql = db.get("mysql");
            // D_M.setMysqlDbData(mysql);
        } catch (IOException e) {
            System.err.println("Imposible de lire le JSON");
            e.printStackTrace();
        }
    }

    /**
     * Sauvegarder toutes les préférences de l'utilisateur au fermement de
     * l'application
     */
    public void save() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Map<String, Object> root = new HashMap<>();
        // root.put("last_used_directory", F_M.getLastUserDirectory());
        root.put("theme_type", T_M.getCurrentThemeType());

        Map<String, String> themes = new HashMap<>();
        PersoTheme pt = (PersoTheme) T_M.getTheme(ThemeManager.ThemeType.PERSO);
        themes.put("background_color", pt.getBackgroundColor());
        themes.put("card_color", pt.getCardColor());
        themes.put("border_color", pt.getBorderColor());
        themes.put("selection_border_color", pt.getSelectionBorderColor());
        themes.put("header_color", pt.getHeaderColor());
        themes.put("text_color", pt.getTextColor());
        themes.put("secondary_text_color", pt.getSecondaryTextColor());
        themes.put("toolbar_color", pt.getToolbarColor());
        themes.put("toolbar_border_color", pt.getToolbarBorderColor());
        root.put("perso_theme", themes);

        // Map<String, Map<String, Object>> db = new HashMap<>();
        // Map<String, Object> mysql = new HashMap<>();
        // mysql.put("host", "localhost");
        // mysql.put("user", "root");
        // mysql.put("password", "");
        // mysql.put("port", "3306");
        // List<Map<String, String>> tables = new ArrayList<>();
        // for (String t : D_M.getSqlTypeDatabases(DbType.MySql)) {
        //     Map<String, String> map = new HashMap<>();
        //     map.put("name", t);
        //     tables.add(map);
        // }
        // mysql.put("databases", tables);
        // db.put("mysql", mysql);
        // root.put("db_type", db);

        try {
            mapper.writeValue(JsonManager.PATH_TO_FILE_USER_DATA.toFile(), root);
            System.out.println("Sauvegarde JSON réussie vers " + JsonManager.PATH_TO_FILE_USER_DATA);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier JSON");
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
        // root.put("last_used_directory", System.getProperty("user.dir"));
        root.put("theme_type", ThemeManager.ThemeType.DARK);

        Map<String, String> themes = new HashMap<>();
        themes.put("background_color", "#333");
        themes.put("card_color", "#444");
        themes.put("border_color", "#555");
        themes.put("selection_border_color", "#666");
        themes.put("header_color", "#777");
        themes.put("text_color", "#888");
        themes.put("secondary_text_color", "#999");
        themes.put("toolbar_color", "#aaa");
        themes.put("toolbar_border_color", "#bbb");
        root.put("perso_theme", themes);

        // Map<String, Map<String, Object>> db = new HashMap<>();
        // Map<String, Object> mysql = new HashMap<>();
        // mysql.put("host", "localhost");
        // mysql.put("user", "root");
        // mysql.put("password", "");
        // mysql.put("port", "3306");
        // mysql.put("databases", new ArrayList<Map<String, String>>());
        // db.put("mysql", mysql);
        // root.put("db_type", db);

        try {
            mapper.writeValue(JsonManager.PATH_TO_FILE_USER_DATA.toFile(), root);
            System.out.println("Sauvegarde JSON réussie vers " + JsonManager.PATH_TO_FILE_USER_DATA);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier JSON");
            e.printStackTrace();
        }
    }

}
