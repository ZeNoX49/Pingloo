package com.dbeditor.model;

public class ForeignKey {
    private String fkName;
    private String columnName;
    private String referencedTable;
    private String referencedColumn;

    public ForeignKey(String fkName, String columnName, String referencedTable, String referencedColumn) {
        this.fkName = fkName;
        this.columnName = columnName;
        this.referencedTable = referencedTable;
        this.referencedColumn = referencedColumn;
    }

    public String getFkName() { return this.fkName; }
    public String getColumnName() { return this.columnName; }
    public String getReferencedTable() { return this.referencedTable; }
    public String getReferencedColumn() { return this.referencedColumn; }
}
