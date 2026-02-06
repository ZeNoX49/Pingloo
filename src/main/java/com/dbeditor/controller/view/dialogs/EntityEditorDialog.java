package com.dbeditor.controller.view.dialogs;

import com.dbeditor.model.Attribute;
import com.dbeditor.model.Entity;
import com.dbeditor.util.ThemeManager;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Dialogue pour créer ou modifier une entité MCD
 */
public class EntityEditorDialog {
    private static final ThemeManager T_M = ThemeManager.getInstance();
    
    private Stage stage;
    private TextField tfEntityName;
    private TableView<AttributeRow> tableAttributes;
    private ObservableList<AttributeRow> attributeData;
    private Entity resultEntity;
    private boolean confirmed = false;

    /**
     * Classe interne pour représenter une ligne d'attribut dans la TableView
     */
    public static class AttributeRow {
        private final SimpleStringProperty name;
        private final SimpleStringProperty type;
        private final SimpleBooleanProperty isIdentifier;

        public AttributeRow(String name, String type, boolean isId) {
            this.name = new SimpleStringProperty(name);
            this.type = new SimpleStringProperty(type);
            this.isIdentifier = new SimpleBooleanProperty(isId);
        }

        public String getName() { return name.get(); }
        public void setName(String value) { name.set(value); }
        public SimpleStringProperty nameProperty() { return name; }

        public String getType() { return type.get(); }
        public void setType(String value) { type.set(value); }
        public SimpleStringProperty typeProperty() { return type; }

        public boolean isIdentifier() { return isIdentifier.get(); }
        public void setIdentifier(boolean value) { isIdentifier.set(value); }
        public SimpleBooleanProperty identifierProperty() { return isIdentifier; }
    }

    /**
     * Constructeur pour créer une nouvelle entité
     */
    public EntityEditorDialog() {
        this(null);
    }

    /**
     * Constructeur pour modifier une entité existante
     * @param entity l'entité à modifier (null pour créer une nouvelle)
     */
    public EntityEditorDialog(Entity entity) {
        this.attributeData = FXCollections.observableArrayList();
        
        // Si on modifie une entité existante, charger ses données
        if (entity != null) {
            for (Attribute attr : entity.getAttributes()) {
                attributeData.add(new AttributeRow(
                    attr.getName(),
                    attr.getType(),
                    attr.isIdentifier()
                ));
            }
        } else {
            // Ajouter une ligne par défaut pour une nouvelle entité
            attributeData.add(new AttributeRow("id", "Identifiant", true));
        }
        
        initUI(entity != null ? entity.getName() : "");
    }

    /**
     * Initialise l'interface utilisateur
     */
    private void initUI(String entityName) {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle(entityName.isEmpty() ? "Créer une entité" : "Modifier l'entité");
        stage.setResizable(true);
        stage.setWidth(700);
        stage.setHeight(500);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + T_M.getTheme().getBackgroundColor() + ";");

        // En-tête avec info
        Label infoLabel = new Label("💡 MCD : Pas de types SQL ni de PK/FK. Juste un identifiant souligné.");
        infoLabel.setStyle(
            "-fx-text-fill: " + T_M.getTheme().getSecondaryTextColor() + "; " +
            "-fx-font-size: 11px; " +
            "-fx-padding: 5; " +
            "-fx-background-color: " + T_M.getTheme().getCardColor() + "; " +
            "-fx-background-radius: 4;"
        );
        infoLabel.setWrapText(true);

        // Section nom de l'entité
        Label lblEntityName = new Label("Nom de l'entité:");
        lblEntityName.setStyle("-fx-text-fill: " + T_M.getTheme().getTextColor() + "; -fx-font-size: 14;");
        
        tfEntityName = new TextField(entityName);
        tfEntityName.setPromptText("Ex: CLIENT, PRODUIT, COMMANDE");
        tfEntityName.setStyle(
            "-fx-background-color: " + T_M.getTheme().getCardColor() + "; " +
            "-fx-text-fill: " + T_M.getTheme().getTextColor() + "; " +
            "-fx-border-color: " + T_M.getTheme().getBorderColor() + "; " +
            "-fx-border-radius: 4; -fx-background-radius: 4;"
        );

        // TableView pour les attributs
        Label lblAttributes = new Label("Attributs:");
        lblAttributes.setStyle("-fx-text-fill: " + T_M.getTheme().getTextColor() + "; -fx-font-size: 14;");
        
        tableAttributes = new TableView<>();
        tableAttributes.setEditable(true);
        tableAttributes.setItems(attributeData);
        tableAttributes.setStyle(
            "-fx-background-color: " + T_M.getTheme().getCardColor() + "; " +
            "-fx-border-color: " + T_M.getTheme().getBorderColor() + ";"
        );
        
        setupTableColumns();

        // Boutons d'action pour les attributs
        HBox attributeActions = new HBox(10);
        attributeActions.setAlignment(Pos.CENTER_LEFT);
        
        Button btnAddAttribute = new Button("Ajouter attribut");
        styleButton(btnAddAttribute, T_M.getTheme().getHeaderColor());
        btnAddAttribute.setOnAction(e -> addAttribute());
        
        Button btnRemoveAttribute = new Button("Supprimer attribut");
        styleButton(btnRemoveAttribute, "#c44545");
        btnRemoveAttribute.setOnAction(e -> removeSelectedAttribute());
        
        attributeActions.getChildren().addAll(btnAddAttribute, btnRemoveAttribute);

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

        VBox.setVgrow(tableAttributes, Priority.ALWAYS);
        root.getChildren().addAll(infoLabel, lblEntityName, tfEntityName, 
                                  lblAttributes, tableAttributes, attributeActions, footer);

        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    /**
     * Configure les colonnes de la TableView
     */
    private void setupTableColumns() {
        // Colonne Nom
        TableColumn<AttributeRow, String> colName = new TableColumn<>("Nom");
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colName.setCellFactory(TextFieldTableCell.forTableColumn());
        colName.setOnEditCommit(e -> e.getRowValue().setName(e.getNewValue()));
        colName.setPrefWidth(200);

        // Colonne Type (conceptuel)
        TableColumn<AttributeRow, String> colType = new TableColumn<>("Type conceptuel");
        colType.setCellValueFactory(data -> data.getValue().typeProperty());
        colType.setCellFactory(TextFieldTableCell.forTableColumn());
        colType.setOnEditCommit(e -> e.getRowValue().setType(e.getNewValue()));
        colType.setPrefWidth(200);

        // Colonne Identifiant (checkbox)
        TableColumn<AttributeRow, Boolean> colId = new TableColumn<>("Identifiant");
        colId.setCellValueFactory(data -> data.getValue().identifierProperty());
        colId.setCellFactory(CheckBoxTableCell.forTableColumn(colId));
        colId.setPrefWidth(100);
        
        // Ajouter un listener pour s'assurer qu'un seul identifiant est sélectionné
        colId.setOnEditCommit(e -> {
            if (e.getNewValue()) {
                // Désélectionner tous les autres
                for (AttributeRow row : attributeData) {
                    if (row != e.getRowValue()) {
                        row.setIdentifier(false);
                    }
                }
            }
        });

        tableAttributes.getColumns().addAll(colName, colType, colId);
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
     * Ajoute un nouvel attribut vide
     */
    private void addAttribute() {
        attributeData.add(new AttributeRow("nouvel_attribut", "Texte", false));
    }

    /**
     * Supprime l'attribut sélectionné
     */
    private void removeSelectedAttribute() {
        AttributeRow selected = tableAttributes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            attributeData.remove(selected);
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner un attribut à supprimer.");
        }
    }

    /**
     * Valide et sauvegarde les données
     */
    private boolean validateAndSave() {
        String name = tfEntityName.getText().trim();
        
        if (name.isEmpty()) {
            showAlert("Erreur", "Le nom de l'entité ne peut pas être vide.");
            return false;
        }
        
        if (attributeData.isEmpty()) {
            showAlert("Erreur", "L'entité doit contenir au moins un attribut.");
            return false;
        }

        // Vérifier qu'au moins un identifiant existe
        boolean hasIdentifier = false;
        for (AttributeRow row : attributeData) {
            if (row.isIdentifier()) {
                hasIdentifier = true;
                break;
            }
        }
        
        if (!hasIdentifier) {
            showAlert("Erreur", "L'entité doit avoir au moins un identifiant.");
            return false;
        }

        // Vérifier que tous les attributs ont un nom
        for (AttributeRow row : attributeData) {
            if (row.getName().trim().isEmpty()) {
                showAlert("Erreur", "Tous les attributs doivent avoir un nom.");
                return false;
            }
        }

        // Créer l'entité résultat
        resultEntity = new Entity(name);
        for (AttributeRow row : attributeData) {
            Attribute attr = new Attribute(row.getName(), row.getType());
            attr.setIdentifier(row.isIdentifier());
            resultEntity.addAttribute(attr);
            
            // Marquer l'identifiant au niveau de l'entité
            if (row.isIdentifier()) {
                resultEntity.setIdentifier(row.getName());
            }
        }

        return true;
    }

    /**
     * Affiche une alerte
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
     * Retourne l'entité créée/modifiée
     */
    public Entity getResultEntity() {
        return resultEntity;
    }
}