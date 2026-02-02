package com.dbeditor.controller.view.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Modèle simple de sélection réutilisable.
 * T : type d'élément (ici TableController)
 */
public class SelectionModel<T> {
    private final List<T> selected = new ArrayList<>();
    // callback pour appliquer visuellement la sélection : (item, selected?)
    private final BiConsumer<T, Boolean> visualizer;

    public SelectionModel(BiConsumer<T, Boolean> visualizer) {
        this.visualizer = visualizer;
    }

    public void select(T item) {
        if (!selected.contains(item)) {
            selected.add(item);
            visualizer.accept(item, true);
        }
    }

    public void deselect(T item) {
        if (selected.remove(item)) {
            visualizer.accept(item, false);
        }
    }

    public void toggle(T item) {
        if (selected.contains(item)) deselect(item);
        else select(item);
    }

    public void clear() {
        for (T t : new ArrayList<>(selected)) {
            visualizer.accept(t, false);
        }
        selected.clear();
    }

    public List<T> getSelected() {
        return List.copyOf(selected);
    }

    public void selectAll(Collection<T> items) {
        for (T i : items) select(i);
    }

    public boolean contains(T item) {
        return selected.contains(item);
    }
}
