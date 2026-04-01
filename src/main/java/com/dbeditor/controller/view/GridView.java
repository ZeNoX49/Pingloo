package com.dbeditor.controller.view;

import com.dbeditor.controller.view.dialogs.ColumnRow;
import com.dbeditor.util.ThemeManager;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;

// TODO
public abstract class GridView extends View {
    private static final ThemeManager T_M = ThemeManager.getInstance();

    private TableView<ColumnRow> tableColumns;
    private ObservableList<ColumnRow> columnData;

    @Override
    public void initialization(ToolBar toolbar) {
        this.tableColumns = new TableView<>();
        this.tableColumns.setEditable(true);
        this.tableColumns.setItems(this.columnData);

        this.setupTableColumns();

        this.updateStyle();
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
        TableColumn<ColumnRow, String> colType = new TableColumn<>("Description");
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

    @Override
    public void updateStyle() {
        // TODO
    }

    // TODO
    // public TableView getTableView() {
    //     return this.tableView;
    // }

    @Override
    public Pane getRoot() {
        return null;
        // TODO: return this.tableView;
    }
}
