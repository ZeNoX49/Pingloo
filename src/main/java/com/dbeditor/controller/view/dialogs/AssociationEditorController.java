package com.dbeditor.controller.view.dialogs;

import java.util.ArrayList;
import java.util.List;

import com.dbeditor.MainApp;
import com.dbeditor.controller.CanvasController;
import com.dbeditor.controller.view.dialogs.ColumnData.DialogColumnRow;
import com.dbeditor.controller.view.dialogs.ColumnData.EntityParticipationRow;
import com.dbeditor.model.Column;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;
import com.dbeditor.model.type.IntSql;
import com.dbeditor.model.type.__SqlType;
import com.dbeditor.util.ThemeManager;

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
    private static final ThemeManager T_M = ThemeManager.getInstance();
    
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

    private List<Table> availableEntities;
    private List<EntityParticipationRow> participations;

    @FXML
    private void initialize() {
        this.participations = new ArrayList<>();

        this.btnAddEntity.setOnAction(e -> this.addEntityParticipation());
        this.btnDeleteEntity.setOnAction(e -> this.removeLastEntityParticipation());
        this.btnAddAttr.setOnAction(e -> super.addColumn());
        this.btnDeleteAttr.setOnAction(e -> super.removeSelectedColumn());
    }

    public void setData(List<Table> entities, Stage stage, Table association) {
        super.setData(stage, association);
        this.availableEntities = new ArrayList<>(entities);

        this.tfAssociationName.setText(association == null ? "" : association.name);

        // charger les données de l'association
        if(association == null) {
            IntSql intSql = new IntSql();
            super.columnData.add(new DialogColumnRow("id", intSql.getRepr(MainApp.schema.type), true, true, false, true));
        
            // Par défaut : association binaire
            this.addEntityParticipation();
            this.addEntityParticipation();
        } else {
            for (Column col : association.getColumns()) {
                super.columnData.add(new DialogColumnRow(
                    col.name,
                    col.type.getRepr(MainApp.schema.type),
                    col.isPrimaryKey,
                    col.isNotNull,
                    col.isUnique,
                    col.isAutoIncrementing
                ));
            }

            // Chargement des participations existantes
            for (ForeignKey fk : association.getForeignKeys()) {
                EntityParticipationRow row = new EntityParticipationRow(this.availableEntities);
                row.setEntity(MainApp.schema.tables.get(fk.referencedTable));
                row.setCardinality(fk.cardinalityValue);
                this.participations.add(row);
                this.entitiesBox.getChildren().add(row.container);
            }
        }
    }

    /**
     * Ajoute une participation d'entité
     */
    private void addEntityParticipation() {
        if (this.participations.size() >= MAX_ENTITIES) {
            CanvasController.showWarningAlert("Limite atteinte", "Maximum " + MAX_ENTITIES + " entités pour une association.");
            return;
        }
        
        EntityParticipationRow row = new EntityParticipationRow(this.availableEntities);
        this.participations.add(row);
        this.entitiesBox.getChildren().add(row.container);
    }

    /**
     * Retire la dernière participation
     */
    private void removeLastEntityParticipation() {
        if (this.participations.size() <= MIN_ENTITIES) {
            CanvasController.showWarningAlert("Erreur", "Une association doit avoir au minimum " + MIN_ENTITIES + " entités.");
            return;
        }
        
        EntityParticipationRow last = participations.remove(participations.size() - 1);
        this.entitiesBox.getChildren().remove(last.container);
    }

    @Override
    public void updateStyle() {
        super.updateStyle();
        // TODO
        this.lName.setStyle(this.lName.getStyle());
        this.tfAssociationName.setStyle(this.tfAssociationName.getStyle());
        this.lEntity.setStyle(this.lEntity.getStyle());
        this.entitiesBox.setStyle(this.entitiesBox.getStyle());
        this.btnAddEntity.setStyle(this.btnAddEntity.getStyle());
        this.btnDeleteEntity.setStyle(this.btnDeleteEntity.getStyle());
        this.btnAddAttr.setStyle(this.btnAddAttr.getStyle());
        this.btnDeleteAttr.setStyle(this.btnDeleteAttr.getStyle());
    }

    @Override
    protected boolean validateAndSave() {
        String name = this.tfAssociationName.getText().trim();
        
        if (name.isEmpty()) {
            CanvasController.showWarningAlert("Erreur", "Le nom de l'association ne peut pas être vide.");
            return false;
        }

        // Vérifier que toutes les entités sont sélectionnées
        for (EntityParticipationRow row : this.participations) {
            if (row.getEntity() == null) {
                CanvasController.showWarningAlert("Erreur", "Veuillez sélectionner une entité pour chaque participation.");
                return false;
            }
        }

        // Vérifier qu'il n'y a pas de doublon (sauf pour association réflexive)
        List<Table> selectedEntities = new ArrayList<>();
        for (EntityParticipationRow row : this.participations) {
            if (selectedEntities.contains(row.getEntity())) {
                CanvasController.showWarningAlert("Erreur", "Une même entité ne peut pas apparaître plusieurs fois (sauf pour les associations binaires réflexives).");
                return false;
            }
            selectedEntities.add(row.getEntity());
        }

        // Vérifier que les noms d'attributs ne sont pas vides
        for (DialogColumnRow row : super.columnData) {
            if (row.getName().isBlank()) {
                CanvasController.showWarningAlert("Attribut invalide", "Tous les attributs doivent avoir un nom.");
                return false;
            }
        }

        return true;
    }

    @Override
    protected void buildTable() {
        String name = this.tfAssociationName.getText().trim();
        
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
        this.resultTable.foreignKeys.clear();
        for (EntityParticipationRow row : this.participations) {
            Table entity = row.getEntity();
            Column cPk = null;
            for(Column c : entity.getColumns()) {
                if(c.isPrimaryKey) {
                    cPk = c;
                    break;
                }
            }
            if(cPk == null) continue;

            ForeignKey fk = new ForeignKey(entity.name+"_"+name, cPk.name, entity.name, cPk.name, row.getCardinality());
            fk.isPrimaryKey = true;
            this.resultTable.addForeignKey(fk);
        }
    }
}