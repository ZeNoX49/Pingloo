package com.dbeditor.controller.view.dialogs;

import com.dbeditor.model.Column;
import com.dbeditor.model.Table;
import com.dbeditor.util.ThemeManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Dialogue pour créer ou modifier une table
 */
public class TableEditorDialog {
    private static final ThemeManager T_M = ThemeManager.getInstance();
    
    private Stage stage;
    private TextField tfTableName;
    private TableView<ColumnRow> tableColumns;
    private ObservableList<ColumnRow> columnData;
    private Table resultTable;
    private boolean confirmed = false;

    /**
     * Classe interne pour représenter une ligne de colonne dans la TableView
     */
    public static class ColumnRow {
        private final SimpleStringProperty name;
        private final SimpleStringProperty type;
        private final SimpleBooleanProperty primaryKey;
        private final SimpleBooleanProperty notNull;
        private final SimpleBooleanProperty unique;
        private final SimpleBooleanProperty autoIncrement;

        public ColumnRow(String name, String type, boolean pk, boolean nn, boolean uq, boolean ai) {
            this.name = new SimpleStringProperty(name);
            this.type = new SimpleStringProperty(type);
            this.primaryKey = new SimpleBooleanProperty(pk);
            this.notNull = new SimpleBooleanProperty(nn);
            this.unique = new SimpleBooleanProperty(uq);
            this.autoIncrement = new SimpleBooleanProperty(ai);
        }

        public String getName() { return name.get(); }
        public void setName(String value) { name.set(value); }
        public SimpleStringProperty nameProperty() { return name; }

        public String getType() { return type.get(); }
        public void setType(String value) { type.set(value); }
        public SimpleStringProperty typeProperty() { return type; }

        public boolean isPrimaryKey() { return primaryKey.get(); }
        public void setPrimaryKey(boolean value) { primaryKey.set(value); }
        public SimpleBooleanProperty primaryKeyProperty() { return primaryKey; }

        public boolean isNotNull() { return notNull.get(); }
        public void setNotNull(boolean value) { notNull.set(value); }
        public SimpleBooleanProperty notNullProperty() { return notNull; }

        public boolean isUnique() { return unique.get(); }
        public void setUnique(boolean value) { unique.set(value); }
        public SimpleBooleanProperty uniqueProperty() { return unique; }

        public boolean isAutoIncrement() { return autoIncrement.get(); }
        public void setAutoIncrement(boolean value) { autoIncrement.set(value); }
        public SimpleBooleanProperty autoIncrementProperty() { return autoIncrement; }
    }

    /**
     * Constructeur pour créer une nouvelle table
     */
    public TableEditorDialog() {
        this(null);
    }

    /**
     * Constructeur pour modifier une table existante
     * @param table la table à modifier (null pour créer une nouvelle)
     */
    public TableEditorDialog(Table table) {
        this.columnData = FXCollections.observableArrayList();
        
        // Si on modifie une table existante, charger ses données
        if (table != null) {
            for (Column col : table.getColumns()) {
                columnData.add(new ColumnRow(
                    col.getName(),
                    col.getType(),
                    col.isPrimaryKey(),
                    col.isNotNull(),
                    col.isUnique(),
                    col.isAutoIncrementing()
                ));
            }
        } else {
            // Ajouter une ligne vide par défaut pour une nouvelle table
            columnData.add(new ColumnRow("id", "INT", true, true, false, true));
        }
        
        initUI(table != null ? table.getName() : "");
    }

    /**
     * Initialise l'interface utilisateur
     */
    private void initUI(String tableName) {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle(tableName.isEmpty() ? "Créer une table" : "Modifier la table");
        stage.setResizable(true);
        stage.setWidth(900);
        stage.setHeight(600);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + T_M.getTheme().getBackgroundColor() + ";");

        // Section nom de la table
        Label lblTableName = new Label("Nom de la table:");
        lblTableName.setStyle("-fx-text-fill: " + T_M.getTheme().getTextColor() + "; -fx-font-size: 14;");
        
        tfTableName = new TextField(tableName);
        tfTableName.setPromptText("Entrez le nom de la table");
        tfTableName.setStyle(
            "-fx-background-color: " + T_M.getTheme().getCardColor() + "; " +
            "-fx-text-fill: " + T_M.getTheme().getTextColor() + "; " +
            "-fx-border-color: " + T_M.getTheme().getBorderColor() + "; " +
            "-fx-border-radius: 4; -fx-background-radius: 4;"
        );

        // TableView pour les colonnes
        Label lblColumns = new Label("Colonnes:");
        lblColumns.setStyle("-fx-text-fill: " + T_M.getTheme().getTextColor() + "; -fx-font-size: 14;");
        
        tableColumns = new TableView<>();
        tableColumns.setEditable(true);
        tableColumns.setItems(columnData);
        tableColumns.setStyle(
            "-fx-background-color: " + T_M.getTheme().getCardColor() + "; " +
            "-fx-border-color: " + T_M.getTheme().getBorderColor() + ";"
        );
        
        setupTableColumns();

        // Boutons d'action pour les colonnes
        HBox columnActions = new HBox(10);
        columnActions.setAlignment(Pos.CENTER_LEFT);
        
        Button btnAddColumn = new Button("Ajouter colonne");
        styleButton(btnAddColumn, T_M.getTheme().getHeaderColor());
        btnAddColumn.setOnAction(e -> addColumn());
        
        Button btnRemoveColumn = new Button("Supprimer colonne");
        styleButton(btnRemoveColumn, "#c44545");
        btnRemoveColumn.setOnAction(e -> removeSelectedColumn());
        
        columnActions.getChildren().addAll(btnAddColumn, btnRemoveColumn);

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

        VBox.setVgrow(tableColumns, Priority.ALWAYS);
        root.getChildren().addAll(lblTableName, tfTableName, lblColumns, tableColumns, columnActions, footer);

        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    /**
     * Configure les colonnes de la TableView
     */
    private void setupTableColumns() {
        // Colonne Nom
        TableColumn<ColumnRow, String> colName = new TableColumn<>("Nom");
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colName.setCellFactory(TextFieldTableCell.forTableColumn());
        colName.setOnEditCommit(e -> e.getRowValue().setName(e.getNewValue()));
        colName.setPrefWidth(150);

        // Colonne Type
        TableColumn<ColumnRow, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(data -> data.getValue().typeProperty());
        colType.setCellFactory(TextFieldTableCell.forTableColumn());
        colType.setOnEditCommit(e -> e.getRowValue().setType(e.getNewValue()));
        colType.setPrefWidth(120);

        // Colonne Clé Primaire
        TableColumn<ColumnRow, Boolean> colPK = new TableColumn<>("PK");
        colPK.setCellValueFactory(data -> data.getValue().primaryKeyProperty());
        colPK.setCellFactory(CheckBoxTableCell.forTableColumn(colPK));
        colPK.setPrefWidth(50);

        // Colonne Not Null
        TableColumn<ColumnRow, Boolean> colNN = new TableColumn<>("NOT NULL");
        colNN.setCellValueFactory(data -> data.getValue().notNullProperty());
        colNN.setCellFactory(CheckBoxTableCell.forTableColumn(colNN));
        colNN.setPrefWidth(80);

        // Colonne Unique
        TableColumn<ColumnRow, Boolean> colUnique = new TableColumn<>("UNIQUE");
        colUnique.setCellValueFactory(data -> data.getValue().uniqueProperty());
        colUnique.setCellFactory(CheckBoxTableCell.forTableColumn(colUnique));
        colUnique.setPrefWidth(80);

        // Colonne Auto-Increment
        TableColumn<ColumnRow, Boolean> colAI = new TableColumn<>("AUTO INC");
        colAI.setCellValueFactory(data -> data.getValue().autoIncrementProperty());
        colAI.setCellFactory(CheckBoxTableCell.forTableColumn(colAI));
        colAI.setPrefWidth(90);

        tableColumns.getColumns().addAll(colName, colType, colPK, colNN, colUnique, colAI);
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
     * Ajoute une nouvelle colonne vide
     */
    private void addColumn() {
        columnData.add(new ColumnRow("nouvelle_colonne", "VARCHAR(255)", false, false, false, false));
    }

    /**
     * Supprime la colonne sélectionnée
     */
    private void removeSelectedColumn() {
        ColumnRow selected = tableColumns.getSelectionModel().getSelectedItem();
        if (selected != null) {
            columnData.remove(selected);
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner une colonne à supprimer.");
        }
    }

    /**
     * Valide et sauvegarde les données
     */
    private boolean validateAndSave() {
        String name = tfTableName.getText().trim();
        
        if (name.isEmpty()) {
            showAlert("Erreur", "Le nom de la table ne peut pas être vide.");
            return false;
        }
        
        if (columnData.isEmpty()) {
            showAlert("Erreur", "La table doit contenir au moins une colonne.");
            return false;
        }

        // Vérifier que toutes les colonnes ont un nom
        for (ColumnRow row : columnData) {
            if (row.getName().trim().isEmpty()) {
                showAlert("Erreur", "Toutes les colonnes doivent avoir un nom.");
                return false;
            }
        }

        // Créer la table résultat
        resultTable = new Table(name);
        for (ColumnRow row : columnData) {
            Column col = new Column(row.getName(), row.getType());
            col.setPrimaryKey(row.isPrimaryKey());
            col.setNotNull(row.isNotNull());
            col.setUnique(row.isUnique());
            col.setAutoIncrementing(row.isAutoIncrement());
            resultTable.addColumn(col);
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
     * Retourne la table créée/modifiée
     */
    public Table getResultTable() {
        return resultTable;
    }
}