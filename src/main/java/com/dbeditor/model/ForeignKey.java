package com.dbeditor.model;

import com.dbeditor.model.mcd.CardinalityValue;

public class ForeignKey {
    public String fkName;
    public String columnName;
    public String referencedTable;
    public String referencedColumn;
    public boolean isPrimaryKey;
    public CardinalityValue cardinalityValue;

    public ForeignKey(String fkName, String columnName, String referencedTable, String referencedColumn, CardinalityValue cardinalityValue) {
        this.fkName = fkName;
        this.columnName = columnName;
        this.referencedTable = referencedTable;
        this.referencedColumn = referencedColumn;
        this.isPrimaryKey = false;
        this.cardinalityValue = cardinalityValue;
    }

    public ForeignKey(ForeignKey other) {
        this(other.fkName, other.columnName, other.referencedTable, other.referencedColumn, other.cardinalityValue);
        this.isPrimaryKey = other.isPrimaryKey;
    }
}
