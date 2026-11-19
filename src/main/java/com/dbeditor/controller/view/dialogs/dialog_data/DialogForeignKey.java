package com.dbeditor.controller.view.dialogs.dialog_data;

import java.util.List;
import java.util.function.Function;

import com.dbeditor.manager.Singleton;
import com.dbeditor.manager.ThemeManager;
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

    private static final ThemeManager T_M = Singleton.getInstance(ThemeManager.class);

    private final Function<String, Table> getTable;
    private final ForeignKey foreignKey;
    private final ComboBox<String> entityCombo;
    private final ComboBox<String> cardinalityCombo;
    public final HBox container;
    
    // TODO: style
    public DialogForeignKey(ForeignKey foreignKey, List<String> entitiesName, Function<String, Table> getTable) {
        this.getTable = getTable;

        this.container = new HBox(12);
        this.container.setAlignment(Pos.CENTER_LEFT);
        
        Label lblEntity = new Label("Entité:");
        lblEntity.setPrefWidth(55);
        lblEntity.setStyle("-fx-text-fill: " + T_M.getCurrentTheme().getTextColor() + ";");
        
        this.entityCombo = new ComboBox<>();
        for (String name : entitiesName) {
            this.entityCombo.getItems().add(name);
        }

        this.entityCombo.setPromptText("Sélectionner…");
        this.entityCombo.setPrefWidth(160);
        
        Label lblCard = new Label("Cardinalité:");
        lblCard.setPrefWidth(80);
        lblCard.setStyle("-fx-text-fill: " + T_M.getCurrentTheme().getTextColor() + ";");
        
        this.cardinalityCombo = new ComboBox<>(FXCollections.observableArrayList(
                CardinalityValue._01_.toString(),
                CardinalityValue._11_.toString(),
                CardinalityValue._0N_.toString(),
                CardinalityValue._1N_.toString()
        ));
        this.cardinalityCombo.setPrefWidth(90);
        
        this.container.getChildren().addAll(lblEntity, this.entityCombo, lblCard, this.cardinalityCombo);

        this.foreignKey = foreignKey;
        this.entityCombo.setValue(foreignKey.referencedTable.name);
        this.cardinalityCombo.setValue(foreignKey.cardinalityValue.toString());
    }

    // pour la validation
    public String getEntityName() { return this.entityCombo.getValue(); }
    
    public void removeFromTable(Table table) {
        table.removeAttribute(this.foreignKey);
    }

    public ForeignKey buildFk() {
        Table refTable = this.getTable.apply(this.entityCombo.getValue());
        Column refCol = refTable.getPrimaryKeyColumn();
        this.foreignKey.fkName = refCol.name;
        this.foreignKey.referencedTable = refTable;
        this.foreignKey.referencedColumn = refCol;
        this.foreignKey.cardinalityValue = CardinalityValue.getCardinalityValue(this.cardinalityCombo.getValue());
        return this.foreignKey;
    }

}
