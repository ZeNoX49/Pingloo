package com.dbeditor.controller.view;

import com.dbeditor.controller.ViewType;
import com.dbeditor.controller.modifier.Update;
import com.dbeditor.controller.modifier.Visual;

import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Pane;

public abstract class View implements Visual, Update {
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
     * Renvoie le root de la vue pour l'affciher
     */
    public abstract Pane getRoot();

    public Button createButton(String text) {
        Button button = new Button(text);
        return button;
    }
}