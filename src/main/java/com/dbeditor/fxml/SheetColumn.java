package com.dbeditor.fxml;

import java.lang.constant.Constable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Supported type: {@code Boolean}, {@code Character}, {@code String},
 * {@code Byte}, {@code Short},{@code Integer}, {@code Long}, {@code Float},
 * {@code Double}
 */
public class SheetColumn<T extends Constable> {

    public static enum CellType {
        BOOLEAN_CELL, TEXT_CELL, SELECTION_CELL;
    }

    private final CellType cellType;
    protected final Class<T> columnType;

    protected final List<SheetCell<T>> rows = new ArrayList<>();
    private String[] selectionValue;
    private boolean isNonNull = false;
    private boolean isUnique = false;
    private Function<Integer, T> isAutoIncrement;
    private List<BiConsumer<Integer, T>> rowsListeners = new ArrayList<>();
    private Function<T, Boolean> rowsValidator;
    private T disabledValue;

    public SheetColumn(CellType cellType, Class<T> columnType, String... selectionValue) {
        this.cellType = cellType;
        this.columnType = columnType;
        this.selectionValue = cellType == CellType.SELECTION_CELL ? selectionValue : null;
    }

}
