package com.dbeditor.controller.view.dialogs.dialog_builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.dbeditor.controller.CanvasController;
import com.dbeditor.controller.view.dialogs.dialog_data.DialogForeignKey;
import com.dbeditor.model.CardinalityValue;
import com.dbeditor.model.Column;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ForeignKeyBuilder {

    // private static final ThemeManager T_M = Singleton.getInstance(ThemeManager.class);

    private static final int MIN_ENTITIES = 2;
    private static final int MAX_ENTITIES = 4;

    private static class Container {

        private final VBox vbox;

        public Container(VBox v, Button add, Button delete) {
            Label label = new Label("Entités participantes (2 - 4)");
            
            HBox hbox = new HBox(add, delete);
            hbox.setSpacing(8);
            hbox.setAlignment(Pos.CENTER);

            this.vbox = new VBox(label, v, hbox);
            this.vbox.setSpacing(15);
            this.vbox.setAlignment(Pos.CENTER);
        }

        public VBox getContainer() {
            return this.vbox;
        }

    }

    private final VBox entitiesBox;
    private final Button addEntity;
    private final Button deleteLastEntity;
    private final Container container;
    
    private final List<String> entitiesName;
    private final Function<String, Table> getTable;

    private final List<DialogForeignKey> participations;
    private final Map<DialogForeignKey, Boolean> existingParticipations;
    private final List<DialogForeignKey> participationsToDelete;

    public ForeignKeyBuilder(List<String> entitiesName, Function<String, Table> getTable) {
        this.entitiesName = entitiesName;
        this.getTable = getTable;

        this.participations = new ArrayList<>();
        this.existingParticipations = new HashMap<>();
        this.participationsToDelete = new ArrayList<>();

        this.entitiesBox = new VBox();
        this.entitiesBox.setAlignment(Pos.CENTER);
        this.addEntity = new Button("Ajouter entité");
        this.deleteLastEntity = new Button("Retirer dernière");
        this.container = new Container(this.entitiesBox, this.addEntity, this.deleteLastEntity);
    
        this.addEntity.setOnAction(e -> this.addEntityParticipation());
        this.deleteLastEntity.setOnAction(e -> this.removeLastEntityParticipation());

        // TODO: style
        this.entitiesBox.setStyle(this.entitiesBox.getStyle());
        this.addEntity.setStyle(this.addEntity.getStyle());
        this.deleteLastEntity.setStyle(this.deleteLastEntity.getStyle());
    }

    public void addBaseForeignKeys(List<ForeignKey> listfk) {
        if (listfk.isEmpty()) {
            this.addEntityParticipation();
            this.addEntityParticipation();
        } else {
            for (ForeignKey fk : listfk) {
                DialogForeignKey row = new DialogForeignKey(fk, this.entitiesName, this.getTable);
                this.participations.add(row);
                this.existingParticipations.put(row, true);
                this.entitiesBox.getChildren().add(row.container);
            }
        }
    }

    private void addEntityParticipation() {
        if (this.participations.size() >= MAX_ENTITIES) {
            CanvasController.showWarningAlert("Limite atteinte", "Maximum " + MAX_ENTITIES + " entités pour une association.");
            return;
        }

        Table refTable = getTable.apply(this.entitiesName.get(0));
        Column refCol = refTable.getPrimaryKeyColumn();
        ForeignKey foreignKey = new ForeignKey("", refCol.name, refTable, refCol, CardinalityValue._0N_);

        DialogForeignKey row = new DialogForeignKey(foreignKey, this.entitiesName, this.getTable);
        this.participations.add(row);
        this.existingParticipations.put(row, false);
        this.entitiesBox.getChildren().add(row.container);
    }

    private void removeLastEntityParticipation() {
        if (this.participations.size() <= MIN_ENTITIES) {
            CanvasController.showWarningAlert("Erreur", "Une association doit avoir au minimum " + MIN_ENTITIES + " entités.");
            return;
        }

        DialogForeignKey last = this.participations.remove(this.participations.size() - 1);
        if (this.existingParticipations.remove(last)) {
            this.participationsToDelete.add(last);
        }
        this.entitiesBox.getChildren().remove(last.container);
    }

    public VBox getContainer() {
        return this.container.getContainer();
    }

    public List<DialogForeignKey> getParticipations() {
        return new ArrayList<>(this.participations);
    }

    public void buildForeignKeys(Table table) {
        for (DialogForeignKey dfk : this.existingParticipations.keySet()) {
            ForeignKey fk = dfk.buildFk();
            if (!this.existingParticipations.get(dfk)) {
                table.addAttribute(fk);
            }
        }

        for (DialogForeignKey dfk : this.participationsToDelete) {
            dfk.removeFromTable(table);
        }
    }
    
}
