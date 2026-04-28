package com.dbeditor.controller.view.dialogs.ColumnData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dbeditor.model.Table;
import com.dbeditor.model.mcd.CardinalityValue;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class EntityParticipationRow {
    private final ComboBox<String> entityCombo;
    private final ComboBox<String> cardinalityCombo;
    public final HBox container;
    private final Map<String, Table> tables;
    
    public EntityParticipationRow(List<Table> entities) {
        this.tables = new HashMap<>();

        this.container = new HBox(12);
        this.container.setAlignment(Pos.CENTER_LEFT);
        
        Label lblEntity = new Label("Entité:");
        lblEntity.setPrefWidth(55);
        
        this.entityCombo = new ComboBox<>();
        for(Table t : entities) {
            this.entityCombo.getItems().add(t.name);
            tables.put(t.name, t);
        }
        this.entityCombo.setPromptText("Sélectionner…");
        this.entityCombo.setPrefWidth(160);
        
        Label lblCard = new Label("Cardinalité:");
        lblCard.setPrefWidth(80);
        
        this.cardinalityCombo = new ComboBox<>(FXCollections.observableArrayList(
            CardinalityValue._01_.toString(),
            CardinalityValue._11_.toString(),
            CardinalityValue._0N_.toString(),
            CardinalityValue._1N_.toString()
        ));
        this.cardinalityCombo.setValue(CardinalityValue._0N_.toString());
        this.cardinalityCombo.setPrefWidth(90);
        
        container.getChildren().addAll(lblEntity, this.entityCombo, lblCard, this.cardinalityCombo);
    }
    
    public Table getEntity() {
        return this.tables.get(this.entityCombo.getValue());
    }
    
    public CardinalityValue getCardinality() {
        return CardinalityValue.getCardinalityValue(this.cardinalityCombo.getValue());
    }

    public void setEntity(Table t) {
        this.entityCombo.setValue(t.name);
    }

    public void setCardinality(CardinalityValue cv) {
        this.cardinalityCombo.setValue(cv.toString());
    }
}
