package com.dbeditor.controller.view.dialogs;

import com.dbeditor.MainApp;
import com.dbeditor.controller.CanvasController;
import com.dbeditor.model.Column;
import com.dbeditor.model.Table;
import com.dbeditor.model.type.IntSql;
import com.dbeditor.model.type.VarcharSql;
import com.dbeditor.model.type.__SqlType;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

/**
 * Dialogue pour créer ou modifier une entité
 */
public class EntityEditorDialog extends EditorDialog {
    // private static final ThemeManager T_M = ThemeManager.getInstance();
    
    private TextField tfTableName;
    private final TableView<DialogColumnRow> tableColumns;
    private final ObservableList<DialogColumnRow> columnData;
    private final Table resultTable;

    /**
     * Constructeur pour créer une nouvelle table
     */
    public EntityEditorDialog() {
        this.tableColumns = new TableView<>();
        this.columnData = FXCollections.observableArrayList();
        this.resultTable = new Table("a renommer");

        // Ajouter une ligne vide par défaut pour une nouvelle table
        IntSql intSql = new IntSql();
        this.columnData.add(new DialogColumnRow("id", intSql.getRepr(MainApp.schema.type), true, true, false, true));
        this.initUI("");
    }

    /**
     * Constructeur pour modifier une table existante
     * @param table la table à modifier, null pour créer une nouvelle
     */
    public EntityEditorDialog(Table table) {
        this.tableColumns = new TableView<>();
        this.columnData = FXCollections.observableArrayList();

        this.resultTable = new Table(table);

        // charger les données de la table
        for (Column col : table.getColumns()) {
            this.columnData.add(new DialogColumnRow(
                col.name,
                col.type.getRepr(MainApp.schema.type),
                col.isPrimaryKey,
                col.isNotNull,
                col.isUnique,
                col.isAutoIncrementing
            ));
        }
        this.initUI(table.name);
    }

    /**
     * Initialise l'interface utilisateur
     */
    private void initUI(String tableName) {
        super.stage = new Stage();
        super.stage.initModality(Modality.APPLICATION_MODAL);
        super.stage.initStyle(StageStyle.UTILITY);
        super.stage.setTitle(tableName.isEmpty() ? "Créer une table" : "Modifier la table");
        super.stage.setResizable(true);
        super.stage.setMinWidth(700);
        super.stage.setMinHeight(450);
        super.stage.setWidth(900);
        super.stage.setHeight(600);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        // Section nom de la table
        Label lblTableName = new Label("Nom de la table");
        
        this.tfTableName = new TextField(tableName);
        this.tfTableName.setPromptText("Entrez le nom de la table");

        // TableView pour les colonnes
        Label lblColumns = new Label("Colonnes");
        
        this.tableColumns.setItems(this.columnData);
        super.setupTableColumns(this.tableColumns);
        VBox.setVgrow(this.tableColumns, Priority.ALWAYS);

        // barre d'actions des colonnes
        HBox columnActions = this.buildColumnActionsBar();

        // footer
        HBox footer = this.buildFooter();

        root.getChildren().addAll(
            lblTableName, this.tfTableName,
            new Separator(),
            lblColumns, this.tableColumns,
            columnActions,
            new Separator(),
            footer
        );
        
        super.stage.setScene(new Scene(root));
    }

    private HBox buildColumnActionsBar() {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);

        Button btnAdd = new Button("＋ Ajouter colonne");
        btnAdd.setDefaultButton(false);
        btnAdd.setOnAction(e -> this.addColumn());

        Button btnRemove = new Button("－ Supprimer");
        btnRemove.setOnAction(e -> this.removeSelectedColumn());

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
     * Ajoute une nouvelle colonne vide
     */
    private void addColumn() {
        VarcharSql varcharSql = new VarcharSql(255);
        this.columnData.add(new DialogColumnRow("nouvelle_colonne", varcharSql.getRepr(MainApp.schema.type), false, false, false, false));
        
        // Sélectionner la nouvelle ligne et démarrer l'édition du nom
        int lastIndex = this.columnData.size() - 1;
        this.tableColumns.getSelectionModel().select(lastIndex);
        this.tableColumns.scrollTo(lastIndex);
    }

    /**
     * Supprime la colonne sélectionnée
     */
    private void removeSelectedColumn() {
        DialogColumnRow selected = this.tableColumns.getSelectionModel().getSelectedItem();
        if (selected == null) {
            CanvasController.showWarningAlert("Aucune sélection", "Veuillez sélectionner une colonne à supprimer.");
            return;
        }
        this.columnData.remove(selected);
    }

    /**
     * Valide et sauvegarde les données
     */
    private boolean validateAndSave() {
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
        for (DialogColumnRow row : this.columnData) {
            if (row.getName().isBlank()) {
                CanvasController.showWarningAlert("Erreur", "Toutes les colonnes doivent avoir un nom.");
                return false;
            }
        }

        // Créer la table résultat
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

        return true;
    }

    /**
     * Retourne la table créée/modifiée
     */
    public Table getResultTable() {
        return this.resultTable;
    }
}