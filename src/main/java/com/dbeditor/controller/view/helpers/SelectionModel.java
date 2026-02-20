package com.dbeditor.controller.view.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Modèle simple de sélection
 */
public class SelectionModel<T> {
    private final List<T> selected = new ArrayList<>();
    // callback pour appliquer visuellement la sélection : (table, selected?)
    private final BiConsumer<T, Boolean> visualizer;

    public SelectionModel(BiConsumer<T, Boolean> visualizer) {
        this.visualizer = visualizer;
    }

    /**
     * Sélectionne la value si elle n'est pas déja sélectionné
     */
    public void select(T value) {
        if (!this.selected.contains(value)) {
            this.selected.add(value);
            this.visualizer.accept(value, true);
        }
    }

    /**
     * Désélectionne la value si elle est déja sélectionné
     */
    public void deselect(T value) {
        if (this.selected.remove(value)) {
            this.visualizer.accept(value, false);
        }
    }

    /**
     * Si la value est sélectionnée, la désélectionne<br>
     * Sinon la sélectionne
     */
    public void toggle(T value) {
        if (this.selected.contains(value)) {
            this.deselect(value);
        } else {
            this.select(value);
        }
    }

    /**
     * Déselectionne toutes les value
     */
    public void clear() {
        for (T table : new ArrayList<>(this.selected)) {
            this.deselect(table);
        }
    }

    /**
     * Retourne toutes les value sélectionnées
     * @return
     */
    public List<T> getSelected() {
        return List.copyOf(this.selected);
    }

    /**
     * Sélectionne une liste de values
     */
    public void selectAll(Collection<T> values) {
        for (T value : values) {
            this.select(value);
        }
    }

    /**
     * Vérifie si la value est déja sélectionné
     * @return bool
     */
    public boolean contains(T value) {
        return this.selected.contains(value);
    }
}
