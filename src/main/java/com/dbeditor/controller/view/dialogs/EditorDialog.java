package com.dbeditor.controller.view.dialogs;

import com.dbeditor.MainApp;
import com.dbeditor.controller.CanvasController;
import com.dbeditor.controller.modifier.Visual;
import com.dbeditor.controller.view.dialogs.ColumnData.DialogColumnRow;
import com.dbeditor.model.Table;
import com.dbeditor.model.type.VarcharSql;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

public abstract class EditorDialog implements Visual {
    
    @FXML private TableView<DialogColumnRow> tableColumns;
    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;

    protected ObservableList<DialogColumnRow> columnData;
    protected Table resultTable;

    protected Stage stage;
    protected boolean confirmed = false;

    /**
     * @param table la table à modifier, null pour créer une nouvelle
     */
    protected void setData(Stage stage, Table table) {
        this.stage = stage;
        this.columnData = FXCollections.observableArrayList();
        this.resultTable = table == null ? new Table("a renommer") : new Table(table);

        this.tableColumns.setItems(this.columnData);
        this.setupTableColumns(this.tableColumns);

        this.btnCancel.setOnAction(e -> {
            this.confirmed = false;
            this.stage.close();
        });
        this.btnConfirm.setOnAction(e -> {
            if(this.validateAndSave()) {
                this.confirmed = true;
                this.stage.close();
            }
        });
    }

    /**
     * Valide et sauvegarde les données
     */
    protected abstract boolean validateAndSave();
    
    @Override
    public void updateStyle() {
        // TODO
        this.tableColumns.setStyle(this.tableColumns.getStyle());
        this.btnCancel.setStyle(this.btnCancel.getStyle());
        this.btnConfirm.setStyle(this.btnConfirm.getStyle());
    }
    
    @Override
    public void updateType() {
        // TODO
    }

     /**
     * Configure les colonnes de la TableView
     */
    protected void setupTableColumns(TableView<DialogColumnRow> tableColumns) {
        // --- Nom ---
        TableColumn<DialogColumnRow, String> colName = new TableColumn<>("Nom");
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colName.setCellFactory(TextFieldTableCell.forTableColumn());
        colName.setOnEditCommit(e -> e.getRowValue().setName(e.getNewValue()));
        colName.setPrefWidth(150);
        colName.setMinWidth(100);

        // --- Type ---
        TableColumn<DialogColumnRow, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(data -> data.getValue().typeProperty());
        colType.setCellFactory(TextFieldTableCell.forTableColumn());
        colType.setOnEditCommit(e -> e.getRowValue().setType(e.getNewValue()));
        colType.setPrefWidth(130);
        colType.setMinWidth(80);

        // --- PK ---
        TableColumn<DialogColumnRow, Boolean> colPK = new TableColumn<>("PK");
        colPK.setCellValueFactory(data -> data.getValue().primaryKeyProperty());
        colPK.setCellFactory(CheckBoxTableCell.forTableColumn(colPK));
        colPK.setStyle("-fx-alignment: CENTER;");
        colPK.setPrefWidth(50);
        colPK.setMaxWidth(60);

        // --- NOT NULL ---
        TableColumn<DialogColumnRow, Boolean> colNN = new TableColumn<>("NN");
        colNN.setCellValueFactory(data -> data.getValue().notNullProperty());
        colNN.setCellFactory(CheckBoxTableCell.forTableColumn(colNN));
        colNN.setStyle("-fx-alignment: CENTER;");
        colNN.setPrefWidth(50);
        colNN.setMaxWidth(60);

        // --- UNIQUE ---
        TableColumn<DialogColumnRow, Boolean> colUQ = new TableColumn<>("UQ");
        colUQ.setCellValueFactory(data -> data.getValue().uniqueProperty());
        colUQ.setCellFactory(CheckBoxTableCell.forTableColumn(colUQ));
        colUQ.setStyle("-fx-alignment: CENTER;");
        colUQ.setPrefWidth(50);
        colUQ.setMaxWidth(60);

        // --- AUTO INCREMENT ---
        TableColumn<DialogColumnRow, Boolean> colAI = new TableColumn<>("AI");
        colAI.setCellValueFactory(data -> data.getValue().autoIncrementProperty());
        colAI.setCellFactory(CheckBoxTableCell.forTableColumn(colAI));
        colAI.setStyle("-fx-alignment: CENTER;");
        colAI.setPrefWidth(50);
        colAI.setMaxWidth(60);

        tableColumns.getColumns().setAll(colName, colType, colPK, colNN, colUQ, colAI);
    }

    protected void addColumn() {
        VarcharSql varcharSql = new VarcharSql(255);
        this.columnData.add(new DialogColumnRow("nouvelle_colonne", varcharSql.getRepr(MainApp.schema.type), false, false, false, false));
        
        // Sélectionner la nouvelle ligne et démarrer l'édition du nom
        int lastIndex = this.columnData.size() - 1;
        this.tableColumns.getSelectionModel().select(lastIndex);
        this.tableColumns.scrollTo(lastIndex);
    }

    protected void removeSelectedColumn() {
        DialogColumnRow selected = this.tableColumns.getSelectionModel().getSelectedItem();
        if (selected == null) {
            CanvasController.showWarningAlert("Aucune sélection", "Veuillez sélectionner une colonne à supprimer.");
            return;
        }
        this.columnData.remove(selected);
    }

    /**
     * Affiche le dialogue et attend la fermeture
     */
    public void showAndWait() {
        this.stage.showAndWait();
    }

    /**
     * Retourne true si l'utilisateur a confirmé
     */
    public boolean isConfirmed() {
        return this.confirmed;
    }

    /**
     * Retourne la table créée/modifiée
     */
    public Table getResultTable() {
        this.buildTable();
        return this.resultTable;
    }

    protected abstract void buildTable();
    
}