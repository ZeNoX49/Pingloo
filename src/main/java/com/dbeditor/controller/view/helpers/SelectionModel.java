package com.dbeditor.controller.view.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import com.dbeditor.controller.modifier.Draggable;

/**
 * Modèle simple de sélection
 */
public class SelectionModel<D extends Draggable> {
    private final List<D> selected = new ArrayList<>();
    // callback pour appliquer visuellement la sélection : (Node, selected?)
    private final BiConsumer<D, Boolean> visualizer;

    public SelectionModel(BiConsumer<D, Boolean> visualizer) {
        this.visualizer = visualizer;
    }

    /**
     * Sélectionne le node si il n'est pas déja sélectionné
     */
    public void select(D node) {
        if (!this.selected.contains(node)) {
            this.selected.add(node);
            this.visualizer.accept(node, true);
        }
    }

    /**
     * Désélectionne le node si il est déja sélectionné
     */
    public void deselect(D node) {
        if (this.selected.remove(node)) {
            this.visualizer.accept(node, false);
        }
    }

    /**
     * Si le node est sélectionnée, le désélectionne<br>
     * Sinon le sélectionne
     */
    public void toggle(D node) {
        if (this.selected.contains(node)) {
            this.deselect(node);
        } else {
            this.select(node);
        }
    }

    /**
     * Déselectionne toutes les value
     */
    public void clear() {
        for (D node : new ArrayList<>(this.selected)) {
            this.deselect(node);
        }
    }

    /**
     * Retourne tout les node sélectionnées
     * @return
     */
    public List<D> getSelected() {
        return List.copyOf(this.selected);
    }

    /**
     * Sélectionne une liste de nodes
     */
    public void selectAll(Collection<D> nodes) {
        for (D node : nodes) {
            this.select(node);
        }
    }

    /**
     * Vérifie si le node est déja sélectionné
     * @return bool
     */
    public boolean contains(D node) {
        return this.selected.contains(node);
    }
}
