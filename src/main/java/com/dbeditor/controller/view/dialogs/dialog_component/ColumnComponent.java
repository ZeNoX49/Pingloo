package com.dbeditor.controller.view.dialogs.dialog_component;

import java.lang.constant.Constable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.dbeditor.controller.CanvasController;
import com.dbeditor.fxml.Sheet;
import com.dbeditor.model.Column;
import com.dbeditor.model.Table;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ColumnComponent {

    // private static final ThemeManager T_M = Singleton.getInstance(ThemeManager.class);

    private static class Container {

        private final VBox vbox;

        public Container(Sheet sheet, Button add, Button delete) {
            HBox hbox = new HBox(add, delete);
            hbox.setSpacing(8);

            this.vbox = new VBox(sheet, hbox);
            this.vbox.setSpacing(15);
        }

        public VBox getContainer() {
            return this.vbox;
        }

    }

    private final Sheet sheet;
    private final Button addColumn;
    private final Button deleteColumn;
    private final Container container;
    
    private final List<Column> columns = new ArrayList<>();
    private final Set<Column> columnsNew = new HashSet<>();
    private final Set<Column> columnsToDelete = new HashSet<>();

    public ColumnComponent() {
        this.sheet = new Sheet(String.class, String.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class);
        
        this.addColumn = new Button("+ Ajouter ligne");
        this.deleteColumn = new Button("- Supprimer ligne");
        this.container = new Container(this.sheet, this.addColumn, this.deleteColumn);

        this.setupSheet();
        
        this.addColumn.setOnAction(e -> this.addColumn());
        this.deleteColumn.setOnAction(e -> this.removeSelectedColumn());

        // TODO: style
        this.sheet.setStyle(this.sheet.getStyle());
        this.addColumn.setStyle(this.addColumn.getStyle());
        this.deleteColumn.setStyle(this.deleteColumn.getStyle());
    }

    /**
     * Configure les colonnes de la TableView
     */
    private void setupSheet() {
        this.sheet.setHeaders("Name", "Type", "Primary Key", "Not Null", "Unique", "Auto Increment");
        this.sheet.setUniqueColumns(0);

        this.sheet.disableColumn(5, null, false);
        this.sheet.addColumnListener(2, (row, value) -> {
            if (Boolean.TRUE.equals(value)) {
                this.sheet.disableColumn(2, row, false);
                this.sheet.enableRow(row, true, true, true, false, false, true);
            } else {
                this.sheet.enableColumn(2);
                this.sheet.enableRow(row, true, true, true, true, true, false);
            }
        });
    }

    public void addBaseColumns(List<Column> columns, boolean createKeyColumnIfEmpty) {
        if (columns.isEmpty()) {
            if (createKeyColumnIfEmpty) {
                // TODO: sql type -> new Int.getRepr(super.dbType)
                Column col = new Column("id", "INT");
                col.setPrimaryKey(true);
                this.addColumn(col, true);
            }
        } else {
            for (Column col : columns) {
                this.addColumn(col, false);
            }
        }
    }
    
    private void addColumn(Column column, boolean isNew) {
        this.sheet.addRow(
            column.name,
            column.type,
            column.isPrimaryKey(),
            column.isNotNull,
            column.isUnique,
            column.isAutoIncrementing
        );

        this.columns.add(column);

        if (isNew) {
            this.columnsNew.add(column);
        }
    }

    private void addColumn() {
        // TODO: sql type -> new VarcharSql(255).getRepr(super.dbType)
        this.addColumn(new Column("nouvelle_colonne", "VARCHAR(255)"), true);
        
        // // Sélectionner la nouvelle ligne et démarrer l'édition du nom
        // int lastIndex = this.columnsData.size() - 1;
        // this.tableColumns.getSelectionModel().select(lastIndex);
        // this.tableColumns.scrollTo(lastIndex);
    }

    private void removeSelectedColumn() {
        // DialogColumnEntry selected = this.tableColumns.getSelectionModel().getSelectedItem();
        // if (selected == null) {
        //     CanvasController.showWarningAlert("Aucune sélection", "Veuillez sélectionner une colonne à supprimer.");
        //     return;
        // }

        // this.columnsData.remove(selected);
        // if (this.existingColumns.remove(selected)) {
        //     this.columnsToDelete.add(selected);
        // }
    }

    public VBox getContainer() {
        return this.container.getContainer();
    }

    public boolean checkValidity(Function<List<Column>, Boolean> check) {
        if (!this.sheet.isSheetValid()) {
            CanvasController.showWarningAlert("Erreur", "Les valeurs ne sont pas toutes valides");
            return false;
        }

        Table temp = new Table("temp");
        this.buildColumns(temp);
        return check.apply(temp.getColumns());
    }

    public void buildColumns(Table table) {
        List<List<Constable>> values = this.sheet.getValues();

        for (int i = 0; i < values.size(); i++) {
            List<Constable> row = values.get(i);
            Column column = this.columns.get(i);

            boolean primaryKey = Boolean.TRUE.equals(row.get(2));

            column.name = (String) row.get(0);
            column.type = (String) row.get(1);

            column.setPrimaryKey(primaryKey);
            column.isNotNull = primaryKey || Boolean.TRUE.equals(row.get(3));
            column.isUnique = primaryKey || Boolean.TRUE.equals(row.get(4));
            column.isAutoIncrementing =
                primaryKey && Boolean.TRUE.equals(row.get(5));

            if (this.columnsNew.contains(column)) {
                table.addAttribute(column);
            }
        }

        this.columnsToDelete.forEach(table::removeAttribute);
    }

}
