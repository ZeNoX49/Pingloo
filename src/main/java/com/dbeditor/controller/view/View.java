package com.dbeditor.controller.view;

import java.io.IOException;

import com.dbeditor.controller.ViewType;
import com.dbeditor.controller.VisualModifier;
import com.dbeditor.model.DatabaseSchema;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Pane;

public abstract class View implements VisualModifier {
    /**
     * Donne le ViewType de la vue actuelle
     */
    public abstract ViewType getViewType();

    /**
     * Permet d'initialiser les bases de la vue,
     * remplace le initialize FXML
     */
    public abstract void initialization(ToolBar toolbar);

    /**
     * Permet de charger une bdd
     * @param dbS -> le DatabaseSchema de la bdd
     */
    public abstract void open(DatabaseSchema dbS) throws IOException;

    /**
     * Permet de mettre à jour les vues lors d'un changement dans une vue
     */
    public abstract void onSync();

    /**
     * Renvoie le root de la vue pour l'affciher
     */
    public abstract Pane getRoot();

    public Button createButton(String text) {
        Button button = new Button(text);
        return button;
    }
}