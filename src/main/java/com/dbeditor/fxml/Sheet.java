package com.dbeditor.fxml;

import java.lang.constant.Constable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dbeditor.fxml.SheetCell.SheetBooleanCell;
import com.dbeditor.fxml.SheetCell.SheetSelectionCell;
import com.dbeditor.fxml.SheetCell.SheetTextCell;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

/**
 * Custom JavaFX sheet.
 *
 * Supported type: {@code Boolean}, {@code Character}, {@code String},
 * {@code Byte}, {@code Short},{@code Integer}, {@code Long}, {@code Float},
 * {@code Double}
 */
public class Sheet extends ScrollPane {

    private static final Set<Class<? extends Constable>> SUPPORTED_TYPES = Set.of(
            Boolean.class, Character.class, String.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class
    );

    // private static final String HEADER_STYLE = "-fx-background-color: #e9edf2; -fx-border-color: #c5cbd2; -fx-border-width: 0 1 1 0;";
    // private static final String SUBHEADER_STYLE = "-fx-background-color: #f3f5f7; -fx-border-color: #c5cbd2; -fx-border-width: 0 1 1 0;";

    private final GridPane grid = new GridPane();

    private final List<Class<? extends Constable>> columnTypes = new ArrayList<>();

    private final List<SheetHeaderCell> headers = new ArrayList<>();
    private boolean hasHeader = false;
    private final List<SheetHeaderCell> subHeaders = new ArrayList<>();
    private boolean hasSubHeader = false;

    private final List<List<SheetCell<? extends Constable>>> rows = new ArrayList<>();
    private final Map<Integer, String[]> selectionCells = new HashMap<>();

    public Sheet(Class<? extends Constable>... types) {
        if (types == null || types.length == 0) {
            throw new IllegalArgumentException("At least one column type is required.");
        }

        for (Class<? extends Constable> type : types) {
            Sheet.validateSupportedType(type);
            this.columnTypes.add(type);
        }

        // TODO: style

        super.setContent(this.grid);
        super.setHbarPolicy(ScrollBarPolicy.NEVER);
        super.setVbarPolicy(ScrollBarPolicy.NEVER);
        super.setFocusTraversable(false);
        // super.setFitToWidth(true);

        // super.hvalueProperty().addListener((o, a, b) -> this.updateSticky());
        // super.vvalueProperty().addListener((o, a, b) -> this.updateSticky());
        // super.viewportBoundsProperty().addListener((o, a, b) -> this.updateSticky());
        // this.grid.widthProperty().addListener((o, a, b) -> this.updateSticky());
        // this.grid.heightProperty().addListener((o, a, b) -> this.updateSticky());

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
        for (Class<? extends Constable> type : this.columnTypes) {
            this.headers.add(this.createHeader(type.getSimpleName(), true));
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

    private SheetHeaderCell createHeader(String text, boolean primary) {
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

    private void render() {
        this.grid.getChildren().clear();

        for (int column = 0; column < this.columnTypes.size(); column++) {
            if (this.hasHeader) {
                this.grid.add(this.headers.get(column).container, column, 0);
            }

            if (this.hasSubHeader) {
                this.grid.add(this.subHeaders.get(column).container, column, this.hasHeader ? 1 : 0);
            }
        }

        int firstColumn = this.getFirstColumnIndex();
        for (int row = 0; row < this.rows.size(); row++) {
            for (int column = 0; column < this.columnTypes.size(); column++) {
                StackPane container = this.rows.get(row).get(column).getContainer();
                this.grid.add(container, column, firstColumn + row);
                // GridPane.setHgrow(container, Priority.ALWAYS);
            }
        }
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

    // ******************************************************
    //                          columns                         
    // ******************************************************

    public void setSelectionColumn(int column, String... values) {
        this.checkColumnIndex(column);

        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("At least one value is required.");
        }

        this.selectionCells.put(column, values);

        for (List<SheetCell<?>> columnRow : this.rows) {
            columnRow.remove(column);
            columnRow.add(column, new SheetSelectionCell(values));
        }
    }

    // public <T extends Constable> void setValidators(Function<T, Boolean>... validators) {
    //     // TODO:
    // }

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
            row.add(this.createCell(column, type, Sheet.defaultValue(type)));
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
            row.add(this.createCell(column, type, Sheet.normalizeValue(type, values[column])));
        }

        this.rows.add(row);
        this.render();
    }

    // *******************************************************
    //                          cells                         
    // *******************************************************

    private <T extends Constable> SheetCell<?> createCell(int column, Class<T> type, Constable initialValue) {
        if (this.selectionCells.containsKey(column)) {
            return new SheetSelectionCell(this.selectionCells.get(column));
        }

        if (initialValue != null && !type.isInstance(initialValue)) {
            throw new IllegalArgumentException("Expected " + type.getSimpleName() + " but received " + initialValue.getClass().getSimpleName());
        }

        if (type == Boolean.class) {
            return new SheetBooleanCell(Boolean.TRUE.equals(initialValue));
        }

        return new SheetTextCell<>(type, type.cast(initialValue));
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

    // // ************************************************************************
    // //                          validation and helpers                         
    // // ************************************************************************

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

    private int getFirstColumnIndex() {
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
