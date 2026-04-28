package com.dbeditor.controller.view.dialogs;

import com.dbeditor.MainApp;
import com.dbeditor.controller.CanvasController;
import com.dbeditor.controller.view.dialogs.ColumnData.DialogColumnRow;
import com.dbeditor.model.Column;
import com.dbeditor.model.Table;
import com.dbeditor.model.type.IntSql;
import com.dbeditor.model.type.__SqlType;
import com.dbeditor.util.ThemeManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Dialogue pour créer ou modifier une entité
 */
public class EntityEditorController extends EditorDialog {
    private static final ThemeManager T_M = ThemeManager.getInstance();

    @FXML private Label lName;
    @FXML private TextField tfTableName;
    @FXML private Button btnAdd;
    @FXML private Button btnDelete;

    @FXML
    private void initialize() {
        this.btnAdd.setOnAction(e -> super.addColumn());
        this.btnDelete.setOnAction(e -> super.removeSelectedColumn());
    }

    @Override
    public void setData(Stage stage, Table table) {
        super.setData(stage, table);

        this.tfTableName.setText(table == null ? "" : table.name);

        // charger les données de la table
        if(table == null) {
            IntSql intSql = new IntSql();
            super.columnData.add(new DialogColumnRow("id", intSql.getRepr(MainApp.schema.type), true, true, false, true));
        } else {
            for (Column col : table.getColumns()) {
                super.columnData.add(new DialogColumnRow(
                    col.name,
                    col.type.getRepr(MainApp.schema.type),
                    col.isPrimaryKey,
                    col.isNotNull,
                    col.isUnique,
                    col.isAutoIncrementing
                ));
            }
        }
    }

    @Override
    public void updateStyle() {
        super.updateStyle();
        // TODO
        this.lName.setStyle(this.lName.getStyle());
        this.tfTableName.setStyle(this.tfTableName.getStyle());
        this.btnAdd.setStyle(this.btnAdd.getStyle());
        this.btnDelete.setStyle(this.btnDelete.getStyle());
    }

    @Override
    protected boolean validateAndSave() {
        String name = this.tfTableName.getText().trim();
        
        if (name.isEmpty()) {
            CanvasController.showWarningAlert("Erreur", "Le nom de la table ne peut pas être vide.");
            return false;
        }
        
        if (this.columnData.isEmpty()) {
            CanvasController.showWarningAlert("Erreur", "La table doit contenir au moins une colonne.");
            return false;
        }

        // Vérifier que toutes les colonnes ont un nom
        for (DialogColumnRow row : this.columnData) {
            if (row.getName().isBlank()) {
                CanvasController.showWarningAlert("Erreur", "Toutes les colonnes doivent avoir un nom.");
                return false;
            }
        }

        return true;
    }

    @Override
    protected void buildTable() {
        String name = this.tfTableName.getText().trim();

        this.resultTable.name = name;
        this.resultTable.columns.clear();
        for (DialogColumnRow row : this.columnData) {
            Column col = new Column(row.getName(), __SqlType.get(row.getType(), MainApp.schema.type));
            col.isPrimaryKey = row.isPrimaryKey();
            col.isNotNull = row.isNotNull();
            col.isUnique = row.isUnique();
            col.isAutoIncrementing = row.isAutoIncrement();
            this.resultTable.addColumn(col);
        }
    }
}