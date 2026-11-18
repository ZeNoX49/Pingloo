package com.dbeditor.controller.view.dialogs;

import java.util.Collection;
import java.util.List;

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
    public EntityEditorController(Stage stage, Table table, Collection<String> existingNames) {
        super(stage, table, existingNames);
    }

    @FXML
    private void initialize() {
        super.initialization();

        this.tfTableName.setText(super.resultTable.name);
        this.tfTableName.textProperty().addListener((obs, old, newVal) -> super.resultTable.name = newVal.trim());

        // charger les données de la table
        List<Column> columns = super.resultTable.getColumns();
        if (columns.isEmpty()) {
            Column col = new Column("id", "INT");
            col.setPrimaryKey(true);
            this.resultTable.addAttribute(col);
            super.columnData.add(new DialogColumnEntry(col));
        } else {
            for (Column col : columns) {
                super.columnData.add(new DialogColumnEntry(col));
            }
        }

        this.btnAdd.setOnAction(e -> super.addColumn());
        this.btnDelete.setOnAction(e -> super.removeSelectedColumn());

        this.lName.setStyle("-fx-text-fill: " + T_M.getCurrentTheme().getTextColor() + ";");
        this.tfTableName.setStyle(this.tfTableName.getStyle());
        this.btnAdd.setStyle(this.btnAdd.getStyle());
        this.btnDelete.setStyle(this.btnDelete.getStyle());
    }

    @Override
    protected boolean isValid() {
        String name = super.resultTable.name;

        if (name.isEmpty()) {
            CanvasController.showWarningAlert("Erreur", "Le nom de l'entité ne peut pas être vide");
            return false;
        }

        if (this.existingNames.contains(name)) {
            CanvasController.showWarningAlert("Erreur", "Le nom de l'entité doit être unique");
            return false;
        }

        if (this.columnData.isEmpty()) {
            CanvasController.showWarningAlert("Erreur", "L'entité doit contenir au moins une colonne");
            return false;
        }

        for (DialogColumnEntry row : this.columnData) {
            if (row.getName().isBlank()) {
                CanvasController.showWarningAlert("Erreur", "Toutes les colonnes doivent avoir un nom");
                return false;
            }
        }

        long nbPK = this.columnData.stream().filter(DialogColumnEntry::isPrimaryKey).count();
        if (nbPK > 1) {
            CanvasController.showWarningAlert("Erreur", "L'entité ne peut avoir qu'une seul clé primaire");
            return false;
        }
        
        return true;
    }

}
