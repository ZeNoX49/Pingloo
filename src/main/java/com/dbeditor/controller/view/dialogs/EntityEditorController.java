package com.dbeditor.controller.view.dialogs;

import java.util.Collection;

import com.dbeditor.controller.CanvasController;
import com.dbeditor.controller.view.dialogs.dialog_component.ColumnComponent;
import com.dbeditor.model.Column;
import com.dbeditor.model.Table;

import javafx.fxml.FXML;
import javafx.stage.Stage;

/**
 * Dialogue pour créer ou modifier une entité
 */
public class EntityEditorController extends EditorDialog {

    private final ColumnComponent columnBuilder;

    /**
     * @param table la table à modifier, null pour créer une nouvelle
     */
    public EntityEditorController(Stage stage, Table table, Collection<String> existingNames) {
        super(stage, table, existingNames);
        this.columnBuilder = new ColumnComponent();
    }

    @FXML
    private void initialize() {
        super.initialization();
        
        super.lName.setText("Nom de la table");
        super.tfName.setPromptText("Entrez le nom de la table");
        
        this.hboxBuilder.getChildren().add(this.columnBuilder.getContainer());
        this.columnBuilder.addBaseColumns(super.resultTable.getColumns(), true);
    }

    @Override
    protected boolean isValid() {
        String name = this.tfName.getText().trim();

        if (name.isEmpty()) {
            CanvasController.showWarningAlert("Erreur", "Le nom de l'entité ne peut pas être vide");
            return false;
        }

        if (this.existingNames.contains(name)) {
            CanvasController.showWarningAlert("Erreur", "Le nom de l'entité doit être unique");
            return false;
        }

        return this.columnBuilder.checkValidity(columnsData -> {
            if (columnsData.isEmpty()) {
                CanvasController.showWarningAlert("Erreur", "L'entité doit contenir au moins une colonne");
                return false;
            }

            for (Column row : columnsData) {
                if (row.name.isBlank()) {
                    CanvasController.showWarningAlert("Erreur", "Toutes les colonnes doivent avoir un nom");
                    return false;
                }
            }

            long nbPK = columnsData.stream().filter(Column::isPrimaryKey).count();
            if (nbPK != 1) {
                CanvasController.showWarningAlert("Erreur", "L'entité ne peut avoir qu'une seul clé primaire");
                return false;
            }

            return true;
        });
    }

    @Override
    protected void buildTable() {
        super.resultTable.name = super.tfName.getText().trim();
        this.columnBuilder.buildColumns(super.resultTable);
    }

}
