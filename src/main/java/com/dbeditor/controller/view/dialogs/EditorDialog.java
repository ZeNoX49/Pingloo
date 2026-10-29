package com.dbeditor.controller.view.dialogs;

import com.dbeditor.controller.CanvasController;
import com.dbeditor.controller.view.dialogs.dialog_data.DialogColumnEntry;
import com.dbeditor.manager.Singleton;
import com.dbeditor.manager.ThemeManager;
import com.dbeditor.model.Column;
import com.dbeditor.model.Table;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public abstract class EditorDialog {
    
    private static final ThemeManager T_M = Singleton.getInstance(ThemeManager.class);

    @FXML private VBox root;
    @FXML private TableView<DialogColumnEntry> tableColumns;
    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;
    
    protected final Stage stage;
    protected final ObservableList<DialogColumnEntry> columnData;
    protected final Table resultTable;
    protected boolean confirmed = false;

    /**
     * @param table la table à modifier, null pour créer une nouvelle
     */
    public EditorDialog(Stage stage, Table table) {
        this.stage = stage;
        this.columnData = FXCollections.observableArrayList();

        if (table == null) {
            this.resultTable = new Table("");
            this.resultTable.addColumn(new Column("id", "INT"));
        } else {
            // TODO: dupli -> this.resultTable = new Table(table);
            this.resultTable = new Table("temp");
        }
    }

    protected void initialization() {
        this.root.setStyle("-fx-background-color: " + T_M.getTheme().getBackgroundColor());
        this.tableColumns.setStyle(this.tableColumns.getStyle());
        this.btnCancel.setStyle(this.btnCancel.getStyle());
        this.btnConfirm.setStyle(this.btnConfirm.getStyle());

        this.tableColumns.setItems(this.columnData);
        this.setupTableColumns();

        this.btnCancel.setOnAction(e -> {
            this.confirmed = false;
            this.stage.close();
        });
        this.btnConfirm.setOnAction(e -> {
            if (this.validateAndSave()) {
                this.confirmed = true;
                this.stage.close();
            }
        });
    }

     /**
     * Configure les colonnes de la TableView
     */
    private void setupTableColumns() {
        // --- Nom ---
        TableColumn<DialogColumnEntry, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colName.setCellFactory(TextFieldTableCell.forTableColumn());
        colName.setOnEditCommit(e -> e.getRowValue().setName(e.getNewValue()));
        colName.setPrefWidth(150);
        colName.setMinWidth(100);

        // --- Type ---
        TableColumn<DialogColumnEntry, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(data -> data.getValue().typeProperty());
        colType.setCellFactory(TextFieldTableCell.forTableColumn());
        colType.setOnEditCommit(e -> e.getRowValue().setType(e.getNewValue()));
        colType.setPrefWidth(130);
        colType.setMinWidth(80);

        // --- PK ---
        TableColumn<DialogColumnEntry, Boolean> colPK = new TableColumn<>("Primary Key");
        colPK.setCellValueFactory(data -> data.getValue().primaryKeyProperty());
        colPK.setCellFactory(CheckBoxTableCell.forTableColumn(colPK));
        colPK.setStyle("-fx-alignment: CENTER;");
        colPK.setPrefWidth(50);
        colPK.setMaxWidth(60);

        // --- NOT NULL ---
        TableColumn<DialogColumnEntry, Boolean> colNN = new TableColumn<>("Not Null");
        colNN.setCellValueFactory(data -> data.getValue().notNullProperty());
        colNN.setCellFactory(CheckBoxTableCell.forTableColumn(colNN));
        colNN.setStyle("-fx-alignment: CENTER;");
        colNN.setPrefWidth(50);
        colNN.setMaxWidth(60);

        // --- UNIQUE ---
        TableColumn<DialogColumnEntry, Boolean> colUQ = new TableColumn<>("Unique");
        colUQ.setCellValueFactory(data -> data.getValue().uniqueProperty());
        colUQ.setCellFactory(CheckBoxTableCell.forTableColumn(colUQ));
        colUQ.setStyle("-fx-alignment: CENTER;");
        colUQ.setPrefWidth(50);
        colUQ.setMaxWidth(60);

        // --- AUTO INCREMENT ---
        TableColumn<DialogColumnEntry, Boolean> colAI = new TableColumn<>("Auto Increment");
        colAI.setCellValueFactory(data -> data.getValue().autoIncrementProperty());
        colAI.setCellFactory(CheckBoxTableCell.forTableColumn(colAI));
        colAI.setStyle("-fx-alignment: CENTER;");
        colAI.setPrefWidth(50);
        colAI.setMaxWidth(60);

        this.tableColumns.getColumns().setAll(colName, colType, colPK, colNN, colUQ, colAI);
    }

    protected void addColumn() {
        // TODO: sql type
        // VarcharSql varcharSql = new VarcharSql(255);
        // this.columnData.add(new DialogEntry("nouvelle_colonne", varcharSql.getRepr(CanvasController.schema.type), false, false, false, false));
        this.columnData.add(new DialogColumnEntry("nouvelle_colonne", "VARCHAR(255)", false, false, false, false));
        
        // Sélectionner la nouvelle ligne et démarrer l'édition du nom
        int lastIndex = this.columnData.size() - 1;
        this.tableColumns.getSelectionModel().select(lastIndex);
        this.tableColumns.scrollTo(lastIndex);
    }

    protected void removeSelectedColumn() {
        DialogColumnEntry selected = this.tableColumns.getSelectionModel().getSelectedItem();
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

    /**
     * Valide et sauvegarde les données
     */
    protected abstract boolean validateAndSave();

    protected abstract void buildTable();
    
}