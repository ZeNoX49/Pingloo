package com.dbeditor.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.smartcardio.Card;

import com.fasterxml.jackson.databind.ObjectMapper;

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

        
    }

    /**
     * Sauvegarder toutes les préférences de l'utilisateur au fermement de l'application
     */
    public void save() {
        // ObjectMapper objectMapper = new ObjectMapper();
        // objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // CollectionPOJO collectionPOJO = new CollectionPOJO();
        // List<DeckPOJO> deckList = new ArrayList<>();

        // for (Deck deck : Collection.getDecks()) {
        //     DeckPOJO deckPOJO = new DeckPOJO();
        //     deckPOJO.title = deck.getTitle();
        //     deckPOJO.imageUrl = deck.getImageUrl();

        //     List<CardPOJO> cardList = new ArrayList<>();

        //     for(Card card : deck.getCards()) {
        //         CardPOJO cardPOJO = new CardPOJO();

        //         cardPOJO.title = card.getTitle();
        //         cardPOJO.description = card.getDescription();
        //         cardPOJO.date = card.getDate();
        //         cardPOJO.imageUrl = card.getImageUrl();

        //         cardList.add(cardPOJO);
        //     }

        //     deckPOJO.cards = cardList.toArray(CardPOJO[]::new);
        //     deckList.add(deckPOJO);
        // }

        // collectionPOJO.decks = deckList.toArray(DeckPOJO[]::new);

        try {
            File file = PATH_TO_FILE_USER_DATA.toFile();
            objectMapper.writeValue(file, collectionPOJO);
            System.out.println("Sauvegarde JSON réussie vers " + PATH);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier JSON : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Permet de créer le fichier s'il n'existe pas
     */
    private void createUserData() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        
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

        mapper.writeValue(PATH_TO_FILE_USER_DATA.toFile(), root);

    }
    
}