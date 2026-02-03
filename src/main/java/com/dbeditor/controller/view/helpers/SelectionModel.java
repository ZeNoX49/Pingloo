package com.dbeditor.controller.view.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import com.dbeditor.controller.TableController;

/**
 * Modèle simple de sélection
 */
public class SelectionModel {
    private final List<TableController> selected = new ArrayList<>();
    // callback pour appliquer visuellement la sélection : (table, selected?)
    private final BiConsumer<TableController, Boolean> visualizer;

    public SelectionModel(BiConsumer<TableController, Boolean> visualizer) {
        this.visualizer = visualizer;
    }

    public void select(TableController table) {
        if (!this.selected.contains(table)) {
            this.selected.add(table);
            this.visualizer.accept(table, true);
        }
    }

    public void deselect(TableController table) {
        if (this.selected.remove(table)) {
            this.visualizer.accept(table, false);
        }
    }

    public void toggle(TableController table) {
        if (this.selected.contains(table)) {
            this.deselect(table);
        } else {
            this.select(table);
        }
    }

    public void clear() {
        for (TableController table : new ArrayList<>(this.selected)) {
            this.visualizer.accept(table, false);
        }
        this.selected.clear();
    }

    public List<TableController> getSelected() {
        return List.copyOf(this.selected);
    }

    public void selectAll(Collection<TableController> tables) {
        for (TableController table : tables) {
            this.select(table);
        }
    }

    public boolean contains(TableController table) {
        return this.selected.contains(table);
    }
}
