package com.dbeditor.fxml;

import java.lang.constant.Constable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.dbeditor.fxml.SheetCell.SheetBooleanCell;
import com.dbeditor.fxml.SheetCell.SheetSelectionCell;
import com.dbeditor.fxml.SheetCell.SheetTextCell;
import com.dbeditor.fxml.SheetColumn.CellType;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

/**
 * Custom JavaFX sheet.
 */
public class Sheet extends ScrollPane {

    // private static final String HEADER_STYLE = "-fx-background-color: #e9edf2; -fx-border-color: #c5cbd2; -fx-border-width: 0 1 1 0;";
    // private static final String SUBHEADER_STYLE = "-fx-background-color: #f3f5f7; -fx-border-color: #c5cbd2; -fx-border-width: 0 1 1 0;";

    private final GridPane grid = new GridPane();

    private final List<SheetHeaderCell> headers = new ArrayList<>();
    private boolean hasHeader = false;
    private final List<SheetHeaderCell> subHeaders = new ArrayList<>();
    private boolean hasSubHeader = false;

    private final List<SheetColumn<? extends Constable>> sheetColumns = new ArrayList<>();

    // TODO: private final List<Double> widthPercent = new ArrayList<>();

    public Sheet(SheetColumn<? extends Constable>... sheetColumns) {
        this.sheetColumns.addAll(Arrays.asList(sheetColumns));

        // TODO: style

        super.setContent(this.grid);
        super.setHbarPolicy(ScrollBarPolicy.NEVER);
        super.setVbarPolicy(ScrollBarPolicy.NEVER);
        super.setFocusTraversable(false);
        // super.setFitToWidth(true);

        // super.hvalueProperty().addListener((o, a, b) -> this.updateHeaderPos());
        // super.vvalueProperty().addListener((o, a, b) -> this.updateHeaderPos());
        // super.viewportBoundsProperty().addListener((o, a, b) -> this.updateHeaderPos());
        // this.grid.widthProperty().addListener((o, a, b) -> this.updateHeaderPos());
        // this.grid.heightProperty().addListener((o, a, b) -> this.updateHeaderPos());

        // super.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);
        // super.addEventHandler(KeyEvent.KEY_TYPED, this::onKeyTyped);

        this.initializeHeaders();
        // this.initializeColumns();
        this.render();
    }

    // ****************************************************************
    //                          initialization                         
    // ****************************************************************

    private void initializeHeaders() {
        for (SheetColumn<?> column : this.sheetColumns) {
            this.headers.add(this.createHeader(column.columnType.getSimpleName(), true));
            this.subHeaders.add(this.createHeader("", false));
        }
    }

    // private void initializeColumns() {
    //     double percent = 100.0 / this.columnTypes.size();

    //     for (int i = 0; i < this.columnTypes.size(); i++) {
    //         ColumnConstraints constraints = new ColumnConstraints();
    //         constraints.setPercentWidth(percent);
    //         constraints.setMinWidth(100);
    //         constraints.setHgrow(Priority.ALWAYS);
    //         this.grid.getColumnConstraints().add(constraints);
    //     }

    //     this.grid.setMinWidth(this.columnTypes.size() * 100.0);
    // }

    private void render() {
        this.grid.getChildren().clear();

        for (int column = 0; column < this.sheetColumns.size(); column++) {
            if (this.hasHeader) {
                this.grid.add(this.headers.get(column).container, column, 0);
            }

            if (this.hasSubHeader) {
                this.grid.add(this.subHeaders.get(column).container, column, this.hasHeader ? 1 : 0);
            }
        }

        int firstRow = this.getFirstRowIndex();
        // for (int row = 0; row < this.rows.size(); row++) {
        //     for (int column = 0; column < this.columnTypes.size(); column++) {
        //         StackPane container = this.rows.get(row).get(column).getContainer();
        //         this.grid.add(container, column, firstRow + row);
        //         // GridPane.setHgrow(container, Priority.ALWAYS);
        //     }
        // }
    }

    private SheetHeaderCell createHeader(String text, boolean primary) {
        // TODO: style

        Label label = new Label(text);
        // label.setMaxWidth(Double.MAX_VALUE);
        // label.setStyle(primary ? "-fx-font-weight: bold;" : "-fx-font-size: 11px; -fx-text-fill: #606a75;");

        StackPane container = new StackPane(label);
        // container.setAlignment(Pos.CENTER_LEFT);
        // container.setPadding(new Insets(6, 8, 6, 8));
        // container.setMinHeight(32);
        // container.setMaxWidth(Double.MAX_VALUE);
        // container.setStyle(primary ? Sheet.HEADER_STYLE : Sheet.SUBHEADER_STYLE);

        return new SheetHeaderCell(label, container);
    }

    // *********************************************************
    //                          headers                         
    // *********************************************************

    public void setHeaders(String... names) {
        this.validateHeaderCount(names);
        this.hasHeader = true;

        for (int i = 0; i < names.length; i++) {
            this.headers.get(i).label.setText(names[i] == null ? "" : names[i]);
        }

        this.render();
    }

    public void setSubHeaders(String... names) {
        this.validateHeaderCount(names);
        this.hasSubHeader = true;

        for (int i = 0; i < names.length; i++) {
            this.subHeaders.get(i).label.setText(names[i] == null ? "" : names[i]);
        }

        this.render();
    }

    private void updateHeaderPos() {
        // TODO:
    }

    // ******************************************************
    //                          columns                         
    // ******************************************************

    public void setSelectionColumn(int column, String... values) {
        this.checkColumnIndex(column);

        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("At least one value is required.");
        }

        this.cellTypes.remove(column);
        this.cellTypes.add(column, CellType.SELECTION_CELL);
        this.selectionCells.put(column, values);

        for (List<SheetCell<? extends Constable>> columnRow : this.rows) {
            columnRow.remove(column);
            columnRow.add(column, new SheetSelectionCell<>(this.columnTypes.get(column), values));
        }
    }

    public void setNonNullColumns(int... columns) {
        if (columns == null || columns.length == 0) {
            throw new IllegalArgumentException("At least one value is required.");
        }

        for (int column : columns) {
            this.checkColumnIndex(column);

            if (this.cellTypes.get(column) == CellType.BOOLEAN_CELL) {
                throw new IllegalArgumentException("Impossible pour les colonnes booléennes (colonne " + column + ")");
            }

            this.nonNullColumns.add(column);

            for (List<SheetCell<? extends Constable>> row : this.rows) {
                this.addNonNullValidator(row.get(column), column);
            }
        }
    }

    public void setUniqueColumns(int... columns) {
        if (columns == null || columns.length == 0) {
            throw new IllegalArgumentException("At least one value is required.");
        }

        for (int column : columns) {
            this.checkColumnIndex(column);

            if (this.cellTypes.get(column) != CellType.TEXT_CELL) {
                throw new IllegalArgumentException("Uniquement possible pour les colonnes de texte (colonne " + column + ")");
            }

            this.uniqueColumns.add(column);

            for (int i = 0; i < this.rows.size(); i++) {
                SheetTextCell<? extends Constable> textCell = (SheetTextCell<? extends Constable>) this.rows.get(i).get(column);
                this.addUniqueValidator(textCell, column, i);
            }
        }
    }

    public void setAutoIncrementColumn(int column, Function<Integer, Constable> getValue) {
        this.checkColumnIndex(column);

        if (this.cellTypes.get(column) != CellType.TEXT_CELL) {
            throw new IllegalArgumentException("Uniquement possible pour les colonnes de texte (colonne " + column + ")");
        }

        this.autoIncrementColumns.put(column, getValue);

        for (List<SheetCell<? extends Constable>> row : this.rows) {
            // TODO: row.get(column).setEnabled(false, getValue.apply(column + 1));
        }
    }

    public void setValidators(Function<Constable, Boolean>... validators) {
        if (validators == null || validators.length != this.columnTypes.size()) {
            throw new IllegalArgumentException("Expected exactly " + this.columnTypes.size() + " validators.");
        }

        this.validators.addAll(Arrays.asList(validators));

        for (List<SheetCell<? extends Constable>> row : this.rows) {
            for (int i = 0; i < validators.length; i++) {
                // TODO: row.get(i).addValidator(validators[i]);
            }
        }
    }

    public void enableColumn(int column) {
        this.checkColumnIndex(column);

        this.disabledColumns.remove(column);
        for (List<SheetCell<? extends Constable>> row : this.rows) {
            row.get(column).setEnabled(true, null);
        }
    }

    /**
     * @param exceptRow {@code null} pour pour désactiver toutes la colonnes
     * @param defaultValue {@code null} pour ne pas changer la valeur actuelle
     */
    public void disableColumn(int column, Integer exceptRow, Constable defaultValue) {
        this.checkColumnIndex(column);
        if (exceptRow != null) {
            this.checkRowIndex(exceptRow);
        }

        this.disabledColumns.put(column, defaultValue);
        for (int i = 0; i < this.rows.size(); i++) {
            if (exceptRow != null && i == exceptRow) {
                continue;
            }

            List<SheetCell<? extends Constable>> row = this.rows.get(i);
            // TODO: row.get(column).setEnabled(false, defaultValue);
        }
    }

    public void addColumnListener(int column, BiConsumer<Integer, Constable> listener) {
        this.checkColumnIndex(column);

        if (!this.listenerColumns.containsKey(column)) {
            this.listenerColumns.put(column, new ArrayList<>());
        }
        this.listenerColumns.get(column).add(listener);

        for (int i = 0; i < this.rows.size(); i++) {
            final int row = i;
            this.rows.get(row).get(column).addListener((newValue) -> listener.accept(row, newValue));
        }
    }

    // *****************************************************************
    //                          columns helpers                         
    // *****************************************************************

    private void addNonNullValidator(SheetCell<? extends Constable> cell, int column) {
        if (cell instanceof SheetTextCell textCell) {
            // TODO: textCell.addListener(newValue -> false);
        } else if (cell instanceof SheetSelectionCell selectionCell) {
            // TODO: selectionCell.addListener(newValue -> !newValue.isEmpty());
        }
    }
    
    private void addUniqueValidator(SheetTextCell<? extends Constable> textCell, int column, int row) {
        textCell.addValidator(newValue -> {
            for (int i = 0; i < this.rows.size(); i++) {
                if (row == i) {
                    continue;
                }

                SheetTextCell<? extends Constable> otherCell = (SheetTextCell<? extends Constable>) this.rows.get(i).get(column);
                if (otherCell.getValue().equals(newValue)) {
                    return false;
                }
            }
            return true;
        });
    }

    // ******************************************************
    //                          rows                         
    // ******************************************************

    /**
     * Adds an empty row.
     */
    public void addRow() {
        List<SheetCell<? extends Constable>> row = new ArrayList<>();

        for (int column = 0; column < this.columnTypes.size(); column++) {
            Class<? extends Constable> type = this.columnTypes.get(column);
            SheetCell<? extends Constable> cell = this.createCell(column, type, Sheet.defaultValue(type));
            this.setupCellModificators(cell, column);
            row.add(cell);
        }

        this.rows.add(row);
        this.render();
    }

    public void addRow(Constable... values) {
        if (values == null) {
            throw new IllegalArgumentException("Values cannot be null.");
        }

        if (values.length != this.columnTypes.size()) {
            throw new IllegalArgumentException("Not the same number of values as this sheet.");
        }

        List<SheetCell<? extends Constable>> row = new ArrayList<>();

        for (int column = 0; column < this.columnTypes.size(); column++) {
            Class<? extends Constable> type = this.columnTypes.get(column);
            SheetCell<? extends Constable> cell = this.createCell(column, type, Sheet.normalizeValue(type, values[column]));
            this.setupCellModificators(cell, column);
            row.add(cell);
        }

        this.rows.add(row);
        this.render();
    }

    private void setupCellModificators(SheetCell<? extends Constable> cell, int column) {
        final int rowNumber = this.rows.size();

        if (this.nonNullColumns.contains(column)) {
            this.addNonNullValidator(cell, column);
        }

        if (this.uniqueColumns.contains(column)) {
            this.addUniqueValidator((SheetTextCell<? extends Constable>) cell, column, rowNumber);
        }

        if (this.autoIncrementColumns.containsKey(column)) {
            // TODO:
        }

        if (this.listenerColumns.containsKey(column)) {
            // TODO:
        }

        if (!this.validators.isEmpty()) {
            // TODO: cell.addValidator(this.validators.get(column));
        }

        if (this.disabledColumns.containsKey(column)) {
            // TODO: cell.setEnabled(false, this.disabledColumns.get(column));
        }
    }

    public void enableRow(int row, Boolean... enabled) {
        this.checkRowIndex(row);

        if (enabled == null || enabled.length != this.columnTypes.size()) {
            throw new IllegalArgumentException("Not the same number of values as this sheet.");
        }

        List<SheetCell<? extends Constable>> cell = this.rows.get(row);
        for (int i = 0; i < this.columnTypes.size(); i++) {
            cell.get(i).setEnabled(enabled[i], null);
        }
    }

    // *******************************************************
    //                          cells                         
    // *******************************************************

    private <T extends Constable> SheetCell<?> createCell(int column, Class<T> type, Constable initialValue) {
        if (initialValue != null && !type.isInstance(initialValue)) {
            throw new IllegalArgumentException("Expected " + type.getSimpleName() + " but received " + initialValue.getClass().getSimpleName());
        }

        return switch(this.cellTypes.get(column)) {
            case BOOLEAN_CELL -> new SheetBooleanCell(Boolean.TRUE.equals(initialValue));
            case TEXT_CELL -> new SheetTextCell<>(type, type.cast(initialValue));
            case SELECTION_CELL -> new SheetSelectionCell<>(type, this.selectionCells.get(column));
            default -> null;
        };
    }

    // ******************************************************************
    //                          value conversion                         
    // ******************************************************************

    private static Constable normalizeValue(Class<? extends Constable> type, Constable value) {
        if (value == null) {
            return Sheet.defaultValue(type);
        }

        if (!type.isInstance(value)) {
            throw new IllegalArgumentException("Expected " + type.getSimpleName() + " but received " + value.getClass().getSimpleName());
        }

        return value;
    }

    private static Constable defaultValue(Class<? extends Constable> type) {
        if (type == Boolean.class) {
            return Boolean.FALSE;
        }

        return null;
    }

    // ******************************************************************
    //                          public value api                         
    // ******************************************************************

    public List<List<Constable>> getValues() {
        List<List<Constable>> values = new ArrayList<>();

        for (int row = 0; row < this.rows.size(); row++) {
            List<Constable> rowValues = new ArrayList<>();

            for (int column = 0; column < this.columnTypes.size(); column++) {
                rowValues.add(this.rows.get(row).get(column).getValue());
            }

            values.add(Collections.unmodifiableList(rowValues));
        }

        return Collections.unmodifiableList(values);
    }

    public boolean isSheetValid() {
        for (List<SheetCell<? extends Constable>> row : this.rows) {
            for (SheetCell<? extends Constable> cell : row) {
                if (!cell.isValid()) {
                    return false;
                }
            }
        }
        return true;
    }

    // ************************************************************************
    //                          validation and helpers                         
    // ************************************************************************

    private static void validateSupportedType(Class<? extends Constable> type) {
        if (!Sheet.SUPPORTED_TYPES.contains(type)) {
            throw new IllegalArgumentException("Unsupported column type: " + (type == null ? "null" : type.getName()));
        }
    }

    private void validateHeaderCount(String[] names) {
        if (names == null || names.length != this.columnTypes.size()) {
            throw new IllegalArgumentException("Expected exactly " + this.columnTypes.size() + " header names.");
        }
    }

    private void checkRowIndex(int row) {
        if (row < 0 || row >= this.rows.size()) {
            throw new IndexOutOfBoundsException("Invalid row index: " + row);
        }
    }

    private void checkColumnIndex(int column) {
        if (column < 0 || column >= this.columnTypes.size()) {
            throw new IndexOutOfBoundsException("Invalid column index: " + column);
        }
    }

    private int getFirstRowIndex() {
        return (this.hasHeader ? 1 : 0) + (this.hasSubHeader ? 1 : 0);
    }

    // ****************************************************************
    //                          internal class                         
    // ****************************************************************
    private class SheetHeaderCell {

        public final Label label;
        public final StackPane container;

        public SheetHeaderCell(Label label, StackPane container) {
            this.label = label;
            this.container = container;
        }

    }

}
