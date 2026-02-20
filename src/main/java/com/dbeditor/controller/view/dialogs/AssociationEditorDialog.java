package com.dbeditor.controller.view.dialogs;

import java.util.ArrayList;
import java.util.List;

import com.dbeditor.controller.CanvasController;
import com.dbeditor.model.Table;
import com.dbeditor.model.mcd.CardinalityValue;
import com.dbeditor.util.ThemeManager;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Dialogue pour créer ou modifier une association MCD
 */
public class AssociationEditorDialog {
    private static final ThemeManager T_M = ThemeManager.getInstance();
    
    private Stage stage;
    private TextField tfAssociationName;
    private VBox entitiesBox;
    private List<EntityParticipationRow> participations;
    private List<Table> availableEntities;
    private Table resultAssociation;
    private boolean confirmed = false;

    /**
     * Ligne représentant la participation d'une entité dans l'association
     */
    private static class EntityParticipationRow {
        ComboBox<Table> entityCombo;
        ComboBox<String> cardinalityCombo;
        HBox container;
        
        public EntityParticipationRow(List<Table> entities) {
            container = new HBox(10);
            container.setAlignment(Pos.CENTER_LEFT);
            
            Label lblEntity = new Label("Entité:");
            lblEntity.setPrefWidth(60);
            
            entityCombo = new ComboBox<>(FXCollections.observableArrayList(entities));
            entityCombo.setPrefWidth(150);
            
            Label lblCard = new Label("Cardinalité:");
            lblCard.setPrefWidth(80);
            
            cardinalityCombo = new ComboBox<>(FXCollections.observableArrayList(
                CardinalityValue._01_.toString(),
                CardinalityValue._11_.toString(),
                CardinalityValue._0N_.toString(),
                CardinalityValue._1N_.toString()
            ));
            cardinalityCombo.setPrefWidth(80);
            cardinalityCombo.setValue(CardinalityValue._0N_.toString()); // Par défaut
            
            container.getChildren().addAll(lblEntity, entityCombo, lblCard, cardinalityCombo);
        }
        
        public Table getEntity() {
            return entityCombo.getValue();
        }
        
        public CardinalityValue getCardinality() {
            return CardinalityValue.getCardinalityValue(cardinalityCombo.getValue());
        }
        
        public HBox getContainer() {
            return container;
        }
    }

    /**
     * Constructeur
     * @param availableEntities liste des entités disponibles pour l'association
     */
    public AssociationEditorDialog(List<Table> availableEntities) {
        this(availableEntities, null);
    }

    /**
     * Constructeur pour modifier une association existante
     * @param availableEntities liste des entités disponibles
     * @param association l'association à modifier (null pour créer une nouvelle)
     */
    public AssociationEditorDialog(List<Table> availableEntities, Table association) {
        this.availableEntities = new ArrayList<>(availableEntities);
        this.participations = new ArrayList<>();
        
        initUI(association);
    }

    /**
     * Initialise l'interface utilisateur
     */
    private void initUI(Table association) {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle(association == null ? "Créer une association" : "Modifier l'association");
        stage.setResizable(true);
        stage.setWidth(600);
        stage.setHeight(500);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + T_M.getTheme().getBackgroundColor() + ";");

        // En-tête avec info
        Label infoLabel = new Label("💡 Association = lien entre entités avec un verbe à l'infinitif et des cardinalités.");
        infoLabel.setStyle(
            "-fx-text-fill: " + T_M.getTheme().getSecondaryTextColor() + "; " +
            "-fx-font-size: 11px; " +
            "-fx-padding: 5; " +
            "-fx-background-color: " + T_M.getTheme().getCardColor() + "; " +
            "-fx-background-radius: 4;"
        );
        infoLabel.setWrapText(true);

        // Section nom de l'association
        Label lblAssociationName = new Label("Nom de l'association (verbe):");
        lblAssociationName.setStyle("-fx-text-fill: " + T_M.getTheme().getTextColor() + "; -fx-font-size: 14;");
        
        tfAssociationName = new TextField(association != null ? association.getName() : "");
        tfAssociationName.setPromptText("Ex: posseder, commander, appartenir");
        tfAssociationName.setStyle(
            "-fx-background-color: " + T_M.getTheme().getCardColor() + "; " +
            "-fx-text-fill: " + T_M.getTheme().getTextColor() + "; " +
            "-fx-border-color: " + T_M.getTheme().getBorderColor() + "; " +
            "-fx-border-radius: 4; -fx-background-radius: 4;"
        );

        // Section participations des entités
        Label lblEntities = new Label("Entités participantes:");
        lblEntities.setStyle("-fx-text-fill: " + T_M.getTheme().getTextColor() + "; -fx-font-size: 14;");
        
        entitiesBox = new VBox(10);
        entitiesBox.setStyle(
            "-fx-background-color: " + T_M.getTheme().getCardColor() + "; " +
            "-fx-border-color: " + T_M.getTheme().getBorderColor() + "; " +
            "-fx-padding: 10; " +
            "-fx-border-radius: 4; -fx-background-radius: 4;"
        );

        // Si on modifie une association, charger les participations
        if (association != null) {
            // TODO: corriger ceci
            CanvasController.showWarningAlert("Erreur", "Pas compatible avec la version actuelle");
            // for (Table entity : association.getParticipations().keySet()) {
            //     EntityParticipationRow row = new EntityParticipationRow(availableEntities);
            //     row.entityCombo.setValue(entity);
            //     row.cardinalityCombo.setValue(association.getCardinality(entity).toString());
            //     participations.add(row);
            //     entitiesBox.getChildren().add(row.getContainer());
            // }
        } else {
            // Par défaut : 2 entités pour une association binaire
            addEntityParticipation();
            addEntityParticipation();
        }

        // Boutons pour gérer les participations
        HBox entityActions = new HBox(10);
        entityActions.setAlignment(Pos.CENTER_LEFT);
        
        Button btnAddEntity = new Button("Ajouter entité");
        styleButton(btnAddEntity, T_M.getTheme().getHeaderColor());
        btnAddEntity.setOnAction(e -> addEntityParticipation());
        
        Button btnRemoveEntity = new Button("Retirer dernière entité");
        styleButton(btnRemoveEntity, "#c44545");
        btnRemoveEntity.setOnAction(e -> removeLastEntityParticipation());
        
        entityActions.getChildren().addAll(btnAddEntity, btnRemoveEntity);

        // Boutons de validation
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnCancel = new Button("Annuler");
        styleButton(btnCancel, "#666666");
        btnCancel.setOnAction(e -> {
            confirmed = false;
            stage.close();
        });
        
        Button btnConfirm = new Button("Confirmer");
        styleButton(btnConfirm, "#4a9eff");
        btnConfirm.setOnAction(e -> {
            if (validateAndSave()) {
                confirmed = true;
                stage.close();
            }
        });
        
        footer.getChildren().addAll(btnCancel, btnConfirm);

        VBox.setVgrow(entitiesBox, Priority.ALWAYS);
        root.getChildren().addAll(infoLabel, lblAssociationName, tfAssociationName, 
                                  lblEntities, entitiesBox, entityActions, footer);

        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    /**
     * Ajoute une participation d'entité
     */
    private void addEntityParticipation() {
        if (participations.size() >= 4) {
            CanvasController.showWarningAlert("Limite atteinte", "Maximum 4 entités pour une association.");
            return;
        }
        
        EntityParticipationRow row = new EntityParticipationRow(availableEntities);
        participations.add(row);
        entitiesBox.getChildren().add(row.getContainer());
    }

    /**
     * Retire la dernière participation
     */
    private void removeLastEntityParticipation() {
        if (participations.size() <= 2) {
            CanvasController.showWarningAlert("Erreur", "Une association doit avoir au minimum 2 entités.");
            return;
        }
        
        EntityParticipationRow last = participations.remove(participations.size() - 1);
        entitiesBox.getChildren().remove(last.getContainer());
    }

    /**
     * Style un bouton
     */
    private void styleButton(Button btn, String bgColor) {
        btn.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 8 15; " +
            "-fx-background-radius: 4; " +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setOpacity(0.8));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
    }

    /**
     * Valide et sauvegarde les données
     */
    private boolean validateAndSave() {
        String name = tfAssociationName.getText().trim();
        
        if (name.isEmpty()) {
            CanvasController.showWarningAlert("Erreur", "Le nom de l'association ne peut pas être vide.");
            return false;
        }
        
        if (participations.size() < 2) {
            CanvasController.showWarningAlert("Erreur", "Une association doit relier au moins 2 entités.");
            return false;
        }

        // Vérifier que toutes les entités sont sélectionnées
        for (EntityParticipationRow row : participations) {
            if (row.getEntity() == null) {
                CanvasController.showWarningAlert("Erreur", "Veuillez sélectionner une entité pour chaque participation.");
                return false;
            }
        }

        // Vérifier qu'il n'y a pas de doublon (sauf pour association réflexive)
        if (participations.size() > 2) {
            List<Table> selectedEntities = new ArrayList<>();
            for (EntityParticipationRow row : participations) {
                if (selectedEntities.contains(row.getEntity())) {
                    CanvasController.showWarningAlert("Erreur", "Une même entité ne peut pas apparaître plusieurs fois " +
                                       "(sauf pour les associations binaires réflexives).");
                    return false;
                }
                selectedEntities.add(row.getEntity());
            }
        }

        // Créer l'association résultat
        resultAssociation = new Table(name);
        // TODO: corriger ceci
        // for (EntityParticipationRow row : participations) {
        //     resultAssociation.addParticipation(row.getEntity(), row.getCardinality());
        // }

        return true;
    }

    /**
     * Affiche le dialogue et attend la fermeture
     */
    public void showAndWait() {
        stage.showAndWait();
    }

    /**
     * Retourne true si l'utilisateur a confirmé
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Retourne l'association créée/modifiée
     */
    public Table getResultAssociation() {
        return resultAssociation;
    }
}