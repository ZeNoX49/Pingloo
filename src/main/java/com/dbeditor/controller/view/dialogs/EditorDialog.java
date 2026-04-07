package com.dbeditor.controller.view.dialogs;

import com.dbeditor.controller.modifier.Visual;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

public abstract class EditorDialog implements Visual {
    
    protected Stage stage;
    protected boolean confirmed = false;

    @Override
    public void updateStyle() {
        // TODO
    }

     /**
     * Configure les colonnes de la TableView
     */
    protected void setupTableColumns(TableView<DialogColumnRow> tableColumns) {
        tableColumns.setEditable(true);
        tableColumns.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableColumns.setPlaceholder(new javafx.scene.control.Label("Aucune colonne. Cliquez sur « Ajouter »."));

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
    
}