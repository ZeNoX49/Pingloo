package com.dbeditor.controller.view.dialogs.dialog_builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dbeditor.controller.CanvasController;
import com.dbeditor.controller.view.dialogs.dialog_data.DialogColumnEntry;
import com.dbeditor.model.Column;
import com.dbeditor.model.Table;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ColumnBuilder {

    // private static final ThemeManager T_M = Singleton.getInstance(ThemeManager.class);

    private static class Container {

        private final VBox vbox;

        public Container(TableView<?> tv, Button add, Button delete) {
            HBox hbox = new HBox(add, delete);
            hbox.setSpacing(8);

            this.vbox = new VBox(tv, hbox);
            this.vbox.setSpacing(15);
        }

        public VBox getContainer() {
            return this.vbox;
        }

    }

    private final TableView<DialogColumnEntry> tableColumns;
    private final Button addColumn;
    private final Button deleteColumn;
    private final Container container;
    
    private final ObservableList<DialogColumnEntry> columnsData;
    private final Map<DialogColumnEntry, Boolean> existingColumns;
    private final List<DialogColumnEntry> columnsToDelete;

    public ColumnBuilder() {
        this.columnsData = FXCollections.observableArrayList();
        this.existingColumns = new HashMap<>();
        this.columnsToDelete = new ArrayList<>();
        
        this.tableColumns = new TableView<>();
        this.addColumn = new Button("+ Ajouter colonne");
        this.deleteColumn = new Button("- Supprimer colonne");
        this.container = new Container(this.tableColumns, this.addColumn, this.deleteColumn);

        this.tableColumns.setItems(this.columnsData);
        this.setupTableColumns();
        
        this.addColumn.setOnAction(e -> this.addColumn());
        this.deleteColumn.setOnAction(e -> this.removeSelectedColumn());

        // TODO: style
        this.tableColumns.setStyle(this.tableColumns.getStyle());
        this.addColumn.setStyle(this.addColumn.getStyle());
        this.deleteColumn.setStyle(this.deleteColumn.getStyle());
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

    public void addBaseColumns(List<Column> columns, boolean createKeyColumnIfEmpty) {
        if (columns.isEmpty()) {
            if (createKeyColumnIfEmpty) {
                Column col = new Column("id", "INT");
                col.setPrimaryKey(true);
                DialogColumnEntry row = new DialogColumnEntry(col);
                this.columnsData.add(row);
                this.existingColumns.put(row, false);
            }
        } else {
            for (Column col : columns) {
                DialogColumnEntry row = new DialogColumnEntry(col);
                this.columnsData.add(row);
                this.existingColumns.put(row, true);
            }
        }
    }

    private void addColumn() {
        // TODO: sql type -> new Column("nouvelle_colonne", new VarcharSql(255).getRepr(CanvasController.schema.type));
        DialogColumnEntry row = new DialogColumnEntry(new Column("nouvelle_colonne", "VARCHAR(255)"));
        this.columnsData.add(row);
        this.existingColumns.put(row, false);
        
        // Sélectionner la nouvelle ligne et démarrer l'édition du nom
        int lastIndex = this.columnsData.size() - 1;
        this.tableColumns.getSelectionModel().select(lastIndex);
        this.tableColumns.scrollTo(lastIndex);
    }

    private void removeSelectedColumn() {
        DialogColumnEntry selected = this.tableColumns.getSelectionModel().getSelectedItem();
        if (selected == null) {
            CanvasController.showWarningAlert("Aucune sélection", "Veuillez sélectionner une colonne à supprimer.");
            return;
        }

        this.columnsData.remove(selected);
        if (this.existingColumns.remove(selected)) {
            this.columnsToDelete.add(selected);
        }
    }

    public VBox getContainer() {
        return this.container.getContainer();
    }

    public ObservableList<DialogColumnEntry> getColunmsData() {
        return this.columnsData;
    }

    public void buildColumns(Table table) {
        for (DialogColumnEntry row : this.existingColumns.keySet()) {
            Column col = row.buildColumn();
            if (!this.existingColumns.get(row)) {
                table.addAttribute(col);
            }
        }

        for (DialogColumnEntry row : this.columnsToDelete) {
            row.removeFromTable(table);
        }
    }

}
