package com.dbeditor.controller.view.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.dbeditor.controller.CanvasController;
import com.dbeditor.controller.view.dialogs.dialog_builder.ColumnBuilder;
import com.dbeditor.controller.view.dialogs.dialog_builder.ForeignKeyBuilder;
import com.dbeditor.controller.view.dialogs.dialog_data.DialogColumnEntry;
import com.dbeditor.controller.view.dialogs.dialog_data.DialogForeignKey;
import com.dbeditor.model.Table;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.stage.Stage;

/**
 * Dialogue pour créer ou modifier une association
 */
public class AssociationEditorController extends EditorDialog {

    private final ColumnBuilder columnBuilder;
    private final ForeignKeyBuilder fkBuilder;

    /**
     * @param table la table à modifier, null pour créer une nouvelle
     */
    public AssociationEditorController(Stage stage, Table table, List<String> entitiesName, Collection<String> existingNames, Function<String, Table> getTable) {
        super(stage, table, existingNames);
        this.columnBuilder = new ColumnBuilder();
        this.fkBuilder = new ForeignKeyBuilder(entitiesName, getTable);
    }

    @FXML
    private void initialize() {
        super.initialization();
        
        super.lName.setText("Nom de l'association (verbe à l'infinitif)");
        super.tfName.setPromptText("ex : posséder, commander, appartenir…");

        this.hboxBuilder.getChildren().addAll(this.fkBuilder.getContainer(), new Separator(Orientation.VERTICAL), this.columnBuilder.getContainer());
        this.columnBuilder.addBaseColumns(super.resultTable.getColumns(), false);
        this.fkBuilder.addBaseForeignKeys(super.resultTable.getForeignKeys());
    }

    @Override
    protected boolean isValid() {
        String name = this.tfName.getText().trim();

        if (name.isEmpty()) {
            CanvasController.showWarningAlert("Erreur", "Le nom de l'association ne peut pas être vide.");
            return false;
        }

        if (this.existingNames.contains(name)) {
            CanvasController.showWarningAlert("Erreur", "Le nom de l'association doit être unique.");
            return false;
        }

        List<DialogForeignKey> participations = this.fkBuilder.getParticipations();

        // Vérifier que toutes les entités sont sélectionnées
        for (DialogForeignKey row : participations) {
            if (row.getEntityName() == null) {
                CanvasController.showWarningAlert("Erreur", "Veuillez sélectionner une entité pour chaque participation.");
                return false;
            }
        }

        // Vérifier qu'il n'y a pas de doublon (sauf pour association réflexive)
        if (participations.size() > 2) {
            List<String> selectedEntities = new ArrayList<>();
            for (DialogForeignKey row : participations) {
                if (selectedEntities.contains(row.getEntityName())) {
                    CanvasController.showWarningAlert("Erreur", "Une même entité ne peut pas apparaître plusieurs fois (sauf pour les associations binaires réflexives).");
                    return false;
                }
                selectedEntities.add(row.getEntityName());
            }
        }

        ObservableList<DialogColumnEntry> columnsData = this.columnBuilder.getColunmsData();

        // Vérifier que les noms d'attributs ne sont pas vides
        for (DialogColumnEntry row : columnsData) {
            if (row.getName().isBlank()) {
                CanvasController.showWarningAlert("Attribut invalide", "Tous les attributs doivent avoir un nom.");
                return false;
            }
        }

        return true;
    }

    @Override
    protected void buildTable() {
        super.resultTable.name = this.tfName.getText().trim();
        this.columnBuilder.buildColumns(super.resultTable);
        this.fkBuilder.buildForeignKeys(super.resultTable);
    }

}
