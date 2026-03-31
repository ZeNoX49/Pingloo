package com.dbeditor.model;

public class ForeignKey {
    public String fkName;
    public String columnName;
    public String referencedTable;
    public String referencedColumn;

    public ForeignKey(String fkName, String columnName, String referencedTable, String referencedColumn) {
        this.fkName = fkName;
        this.columnName = columnName;
        this.referencedTable = referencedTable;
        this.referencedColumn = referencedColumn;
    }

    public ForeignKey(ForeignKey other) {
        this.fkName = other.fkName;
        this.columnName = other.columnName;
        this.referencedTable = other.referencedTable;
        this.referencedColumn = other.referencedColumn;
    }
}
