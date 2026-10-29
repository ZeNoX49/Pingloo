package com.dbeditor.controller.view.dialogs;

import com.dbeditor.controller.CanvasController;
import com.dbeditor.controller.view.dialogs.dialog_data.DialogColumnEntry;
import com.dbeditor.manager.Singleton;
import com.dbeditor.manager.ThemeManager;
import com.dbeditor.model.Column;
import com.dbeditor.model.Table;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Dialogue pour créer ou modifier une entité
 */
public class EntityEditorController extends EditorDialog {

    private static final ThemeManager T_M = Singleton.getInstance(ThemeManager.class);

    @FXML private Label lName;
    @FXML private TextField tfTableName;
    @FXML private Button btnAdd;
    @FXML private Button btnDelete;

    /**
     * @param table la table à modifier, null pour créer une nouvelle
     */
    public EntityEditorController(Stage stage, Table table) {
        super(stage, table);
    }

    @FXML
    private void initialize() {
        super.initialization();

        this.tfTableName.setText(super.resultTable.name);

        // charger les données de la table
        for (Column col : super.resultTable.getColumns()) {
            super.columnData.add(new DialogColumnEntry(
                    col.name,
                    col.type, // TODO: sql type -> col.type.getRepr(CanvasController.schema.type),
                    col.isPrimaryKey,
                    col.isNotNull,
                    col.isUnique,
                    col.isAutoIncrementing
            ));
        }

        this.btnAdd.setOnAction(e -> super.addColumn());
        this.btnDelete.setOnAction(e -> super.removeSelectedColumn());

        this.lName.setStyle("-fx-text-fill: " + T_M.getTheme().getTextColor() + ";");
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
        for (DialogColumnEntry row : this.columnData) {
            if (row.getName().isBlank()) {
                CanvasController.showWarningAlert("Erreur", "Toutes les colonnes doivent avoir un nom.");
                return false;
            }
        }
        return true;
    }
    
    @Override
    protected void buildTable() {
        this.resultTable.name = this.tfTableName.getText().trim();
        this.resultTable.columns.clear();
        for (DialogColumnEntry row : this.columnData) {
            // TODO: sql type -> Column col = new Column(row.getName(), __SqlType.get(row.getType(), CanvasController.schema.type));
            Column col = new Column(row.getName(), row.getType());
            col.isPrimaryKey = row.isPrimaryKey();
            col.isNotNull = row.isNotNull();
            col.isUnique = row.isUnique();
            col.isAutoIncrementing = row.isAutoIncrement();
            this.resultTable.addColumn(col);
        }
    }

}
