package com.dbeditor.controller.view.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.dbeditor.controller.CanvasController;
import com.dbeditor.controller.view.dialogs.dialog_data.DialogColumnEntry;
import com.dbeditor.controller.view.dialogs.dialog_data.DialogForeignKey;
import com.dbeditor.manager.Singleton;
import com.dbeditor.manager.ThemeManager;
import com.dbeditor.model.CardinalityValue;
import com.dbeditor.model.Column;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Dialogue pour créer ou modifier une association
 */
public class AssociationEditorController extends EditorDialog {

    private static final ThemeManager T_M = Singleton.getInstance(ThemeManager.class);

    private static final int MIN_ENTITIES = 2;
    private static final int MAX_ENTITIES = 4;

    @FXML private Label lName;
    @FXML private TextField tfAssociationName;
    @FXML private Label lEntity;
    @FXML private VBox entitiesBox;
    @FXML private Button btnAddEntity;
    @FXML private Button btnDeleteEntity;
    @FXML private Button btnAddAttr;
    @FXML private Button btnDeleteAttr;

    private final List<String> entitiesName;
    private final Function<String, Table> getTable;
    private final List<DialogForeignKey> participations;

    /**
     * @param table la table à modifier, null pour créer une nouvelle
     */
    public AssociationEditorController(Stage stage, Table table, List<String> entitiesName, Collection<String> existingNames, Function<String, Table> getTable) {
        super(stage, table, existingNames);
        this.entitiesName = entitiesName;
        this.getTable = getTable;
        this.participations = new ArrayList<>();
    }

    @FXML
    private void initialize() {
        super.initialization();

        this.tfAssociationName.setText(this.resultTable.name);
        this.tfAssociationName.textProperty().addListener((obs, old, newVal) -> super.resultTable.name = newVal.trim());

        // charger les données de la table
        for (Column col : super.resultTable.getColumns()) {
            super.columnData.add(new DialogColumnEntry(col));
        }

        List<ForeignKey> listfk = super.resultTable.getForeignKeys();
        if (listfk.isEmpty()) {
            this.addEntityParticipation();
            this.addEntityParticipation();
        } else {
            for (ForeignKey fk : listfk) {
                DialogForeignKey row = new DialogForeignKey(fk, this.entitiesName, this.getTable);
                this.participations.add(row);
                this.entitiesBox.getChildren().add(row.container);
            }
        }
    
        this.btnAddEntity.setOnAction(e -> this.addEntityParticipation());
        this.btnDeleteEntity.setOnAction(e -> this.removeLastEntityParticipation());
    
        this.btnAddAttr.setOnAction(e -> super.addColumn());
        this.btnDeleteAttr.setOnAction(e -> super.removeSelectedColumn());

        this.lName.setStyle("-fx-text-fill: " + T_M.getCurrentTheme().getTextColor() + ";");
        this.tfAssociationName.setStyle(this.tfAssociationName.getStyle());
        this.lEntity.setStyle(this.lEntity.getStyle());
        this.entitiesBox.setStyle(this.entitiesBox.getStyle());
        this.btnAddEntity.setStyle(this.btnAddEntity.getStyle());
        this.btnDeleteEntity.setStyle(this.btnDeleteEntity.getStyle());
        this.btnAddAttr.setStyle(this.btnAddAttr.getStyle());
        this.btnDeleteAttr.setStyle(this.btnDeleteAttr.getStyle());
    }

    private void addEntityParticipation() {
        if (this.participations.size() >= MAX_ENTITIES) {
            CanvasController.showWarningAlert("Limite atteinte", "Maximum " + MAX_ENTITIES + " entités pour une association.");
            return;
        }

        Table refTable = getTable.apply(this.entitiesName.get(0));
        Column refCol = refTable.getPrimaryKeyColumn();
        ForeignKey foreignKey = new ForeignKey("", refCol.name, refTable, refCol, CardinalityValue._0N_);
        super.resultTable.addAttribute(foreignKey);

        DialogForeignKey row = new DialogForeignKey(foreignKey, this.entitiesName, this.getTable);
        this.participations.add(row);
        this.entitiesBox.getChildren().add(row.container);
    }

    private void removeLastEntityParticipation() {
        if (this.participations.size() <= MIN_ENTITIES) {
            CanvasController.showWarningAlert("Erreur", "Une association doit avoir au minimum " + MIN_ENTITIES + " entités.");
            return;
        }
        DialogForeignKey last = this.participations.remove(this.participations.size() - 1);
        last.removeFromTable(this.resultTable);
        this.entitiesBox.getChildren().remove(last.container);
    }

    @Override
    protected boolean isValid() {
        String name = super.resultTable.name;

        if (name.isEmpty()) {
            CanvasController.showWarningAlert("Erreur", "Le nom de l'association ne peut pas être vide.");
            return false;
        }

        if (this.existingNames.contains(name)) {
            CanvasController.showWarningAlert("Erreur", "Le nom de l'association doit être unique.");
            return false;
        }

        // Vérifier que toutes les entités sont sélectionnées
        for (DialogForeignKey row : this.participations) {
            if (row.getEntityName() == null) {
                CanvasController.showWarningAlert("Erreur", "Veuillez sélectionner une entité pour chaque participation.");
                return false;
            }
        }

        // Vérifier qu'il n'y a pas de doublon (sauf pour association réflexive)
        if (this.participations.size() > 2) {
            List<String> selectedEntities = new ArrayList<>();
            for (DialogForeignKey row : this.participations) {
                if (selectedEntities.contains(row.getEntityName())) {
                    CanvasController.showWarningAlert("Erreur", "Une même entité ne peut pas apparaître plusieurs fois (sauf pour les associations binaires réflexives).");
                    return false;
                }
                selectedEntities.add(row.getEntityName());
            }
        }

        // Vérifier que les noms d'attributs ne sont pas vides
        for (DialogColumnEntry row : super.columnData) {
            if (row.getName().isBlank()) {
                CanvasController.showWarningAlert("Attribut invalide", "Tous les attributs doivent avoir un nom.");
                return false;
            }
        }

        return true;
    }

}
