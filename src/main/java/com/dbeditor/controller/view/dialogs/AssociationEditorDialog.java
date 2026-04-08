package com.dbeditor.controller.view.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dbeditor.controller.CanvasController;
import com.dbeditor.model.Table;
import com.dbeditor.model.mcd.CardinalityValue;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;

/**
 * Dialogue pour créer ou modifier une association
 */
public class AssociationEditorDialog extends EditorDialog {
    // private static final ThemeManager T_M = ThemeManager.getInstance();
    
    private static final int MIN_ENTITIES = 2;   // Nombre minimum d'entités participantes
    private static final int MAX_ENTITIES = 4;   // Nombre maximum d'entités participantes

    private final List<Table> availableEntities;
    private final List<EntityParticipationRow> participations;
    
    private TextField tfAssociationName;
    private VBox entitiesBox;
    private final TableView<DialogColumnRow> tableAttributes;
    private final ObservableList<DialogColumnRow> attributeData;

    /**
     * Ligne représentant la participation d'une entité dans l'association
     */
    private static class EntityParticipationRow {
        private final ComboBox<String> entityCombo;
        private final ComboBox<String> cardinalityCombo;
        final HBox container;
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

        void setEntity(Table t) {
            this.entityCombo.setValue(t.name);
        }

        void setCardinality(CardinalityValue cv) {
            this.cardinalityCombo.setValue(cv.toString());
        }
    }

    /**
     * Constructeur pour créer ou modifier une association existante
     * @param availableEntities liste des entités disponibles
     * @param association l'association à modifier (null pour en créer une nouvelle)
     */
    public AssociationEditorDialog(List<Table> availableEntities, Pair<String, List<Pair<Table, CardinalityValue>>> association) {
        this.availableEntities = new ArrayList<>(availableEntities);
        this.participations = new ArrayList<>();
        this.tableAttributes = new TableView<>();
        this.attributeData = FXCollections.observableArrayList();
        this.initUI(association);
    }

    /**
     * Initialise l'interface utilisateur
     */
    private void initUI(Pair<String, List<Pair<Table, CardinalityValue>>> association) {
        super.stage = new Stage();
        super.stage.initModality(Modality.APPLICATION_MODAL);
        super.stage.initStyle(StageStyle.UTILITY);
        super.stage.setTitle(association == null ? "Créer une association" : "Modifier l'association");
        super.stage.setResizable(true);
        super.stage.setMinWidth(620);
        super.stage.setMinHeight(560);
        super.stage.setWidth(700);
        super.stage.setHeight(680);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        // --- Nom ---
        Label lblName = new Label("Nom de l'association (verbe à l'infinitif)");

        this.tfAssociationName = new TextField(association != null ? association.getKey() : "");
        this.tfAssociationName.setPromptText("ex : posséder, commander, appartenir…");
        this.tfAssociationName.setMaxWidth(350);

        // --- Entités participantes ---
        Label lblEntities = new Label("Entités participantes  (" + MIN_ENTITIES + " – " + MAX_ENTITIES + ")");

        this.entitiesBox = new VBox(8);

        if (association != null) {
            // Chargement des participations existantes
            CanvasController.showWarningAlert("Attention", "La modification d'une association existante est partiellement prise en charge.");
            for (Pair<Table, CardinalityValue> p : association.getValue()) {
                EntityParticipationRow row = new EntityParticipationRow(this.availableEntities);
                row.setEntity(p.getKey());
                row.setCardinality(p.getValue());
                this.participations.add(row);
                this.entitiesBox.getChildren().add(row.container);
            }
        } else {
            // Par défaut : association binaire
            this.addEntityParticipation();
            this.addEntityParticipation();
        }

        HBox entityActions = this.buildEntityActionsBar();

        // --- Attributs de l'association ---
        Label lblAttributes = new Label("Attributs de l'association (optionnel)");

        this.tableAttributes.setItems(this.attributeData);
        super.setupTableColumns(this.tableAttributes);
        this.tableAttributes.setPrefHeight(180);
        VBox.setVgrow(this.tableAttributes, Priority.ALWAYS);

        HBox attributeActions = this.buildAttributeActionsBar();

        // --- Footer ---
        HBox footer = this.buildFooter();

        root.getChildren().addAll(
            lblName, this.tfAssociationName,
            new Separator(),
            lblEntities, this.entitiesBox, entityActions,
            new Separator(),
            lblAttributes, this.tableAttributes, attributeActions,
            new Separator(),
            footer
        );

        super.stage.setScene(new Scene(root));
    }

    private HBox buildEntityActionsBar() {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);

        Button btnAdd = new Button("＋ Ajouter entité");
        btnAdd.setOnAction(e -> this.addEntityParticipation());

        Button btnRemove = new Button("－ Retirer dernière");
        btnRemove.setOnAction(e -> this.removeLastEntityParticipation());

        bar.getChildren().addAll(btnAdd, btnRemove);
        return bar;
    }

    private HBox buildAttributeActionsBar() {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);

        Button btnAdd = new Button("＋ Ajouter attribut");
        btnAdd.setOnAction(e -> this.addAttribute());

        Button btnRemove = new Button("－ Supprimer");
        btnRemove.setOnAction(e -> this.removeSelectedAttribute());

        bar.getChildren().addAll(btnAdd, btnRemove);
        return bar;
    }

    private HBox buildFooter() {
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(4, 0, 0, 0));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnCancel = new Button("Annuler");
        btnCancel.setOnAction(e -> {
            super.confirmed = false;
            super.stage.close();
        });

        Button btnConfirm = new Button("Confirmer");
        btnConfirm.setDefaultButton(true);
        btnConfirm.setOnAction(e -> {
            if (validateAndSave()) {
                super.confirmed = true;
                super.stage.close();
            }
        });

        footer.getChildren().addAll(spacer, btnCancel, btnConfirm);
        return footer;
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

    private void addAttribute() {
        this.attributeData.add(new DialogColumnRow("attribut", "VARCHAR(255)", false, false, false, false));
        int lastIndex = this.attributeData.size() - 1;
        this.tableAttributes.getSelectionModel().select(lastIndex);
        this.tableAttributes.scrollTo(lastIndex);
    }

    private void removeSelectedAttribute() {
        DialogColumnRow selected = this.tableAttributes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            CanvasController.showWarningAlert("Aucune sélection", "Sélectionnez un attribut à supprimer.");
            return;
        }
        this.attributeData.remove(selected);
    }

    /**
     * Valide et sauvegarde les données
     */
    private boolean validateAndSave() {
        String name = this.tfAssociationName.getText().trim();
        
        if (name.isEmpty()) {
            CanvasController.showWarningAlert("Erreur", "Le nom de l'association ne peut pas être vide.");
            return false;
        }
        
        if (this.participations.size() < MIN_ENTITIES) {
            CanvasController.showWarningAlert("Erreur", "Une association doit relier au moins " + MIN_ENTITIES + " entités.");
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
        if (this.participations.size() > MIN_ENTITIES) {
            List<Table> selectedEntities = new ArrayList<>();
            for (EntityParticipationRow row : this.participations) {
                if (selectedEntities.contains(row.getEntity())) {
                    CanvasController.showWarningAlert("Erreur", "Une même entité ne peut pas apparaître plusieurs fois (sauf pour les associations binaires réflexives).");
                    return false;
                }
                selectedEntities.add(row.getEntity());
            }
        }

        // Vérifier que les noms d'attributs ne sont pas vides
        for (DialogColumnRow row : this.attributeData) {
            if (row.getName().isBlank()) {
                CanvasController.showWarningAlert("Attribut invalide", "Tous les attributs doivent avoir un nom.");
                return false;
            }
        }

        return true;
    }

    /**
     * Retourne les données saisies sous la forme d'une paire :
     * <ul>
     *   <li><b>key</b> : nom de l'association</li>
     *   <li><b>value</b> : liste des participations (nom de l'entité, cardinalité)</li>
     * </ul>
     *
     * <p>Les attributs sont disponibles via {@link #getResultAttributes()}.</p>
     *
     * @return la paire résultat, ou une paire vide si le dialogue n'a pas été confirmé
     */
    public Pair<String, List<Pair<String, CardinalityValue>>> getResultAssociation() {
        if (!super.confirmed) return new Pair<>("", List.of());

        String name = this.tfAssociationName.getText().trim();
        List<Pair<String, CardinalityValue>> parts = new ArrayList<>();
        for (EntityParticipationRow row : this.participations) {
            parts.add(new Pair<>(row.getEntity().name, row.getCardinality()));
        }
        return new Pair<>(name, parts);
    }

    /**
     * @return la liste des attributs définis pour l'association
     */
    public List<DialogColumnRow> getResultAttributes() {
        return new ArrayList<>(this.attributeData);
    }
}