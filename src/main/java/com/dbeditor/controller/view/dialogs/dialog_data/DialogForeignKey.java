package com.dbeditor.controller.view.dialogs.dialog_data;

import java.util.List;
import java.util.function.Function;

import com.dbeditor.model.CardinalityValue;
import com.dbeditor.model.Column;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class DialogForeignKey {

    private final ForeignKey foreignKey;
    private final ComboBox<String> entityCombo;
    private final ComboBox<String> cardinalityCombo;
    public final HBox container;
    
    public DialogForeignKey(ForeignKey foreignKey, List<String> entitiesName, Function<String, Table> getTable) {
        this.container = new HBox(12);
        this.container.setAlignment(Pos.CENTER_LEFT);
        
        Label lblEntity = new Label("Entité:");
        lblEntity.setPrefWidth(55);
        
        this.entityCombo = new ComboBox<>();
        for (String name : entitiesName) {
            this.entityCombo.getItems().add(name);
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
        this.cardinalityCombo.setPrefWidth(90);
        
        container.getChildren().addAll(lblEntity, this.entityCombo, lblCard, this.cardinalityCombo);

        this.foreignKey = foreignKey;
        this.entityCombo.setValue(foreignKey.referencedTable.name);
        this.cardinalityCombo.setValue(foreignKey.cardinalityValue.toString());

        this.entityCombo.valueProperty().addListener((obs, old, newVal) -> {
            Table refTable = getTable.apply(newVal);
            Column refCol = refTable.getPrimaryKeyColumn();
            this.foreignKey.fkName = refCol.name;
            this.foreignKey.referencedTable = refTable;
            this.foreignKey.referencedColumn = refCol;
        });
        this.cardinalityCombo.valueProperty().addListener((obs, old, newVal) -> {
            this.foreignKey.cardinalityValue = CardinalityValue.getCardinalityValue(newVal);
        });
    }

    // pour la validation
    public String getEntityName() { return this.entityCombo.getValue(); }

    
    public void removeFromTable(Table table) {
        table.removeAttribute(this.foreignKey);
    }

}
