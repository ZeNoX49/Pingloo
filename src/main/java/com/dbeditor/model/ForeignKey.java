package com.dbeditor.model;

/**
 * CONSTRAINT {@code fkName} FOREIGN KEY ({@code name}) REFERENCES
 * {@code referencedEntity}({@code referencedColumn})
 */
public class ForeignKey extends Attribut {

    public String fkName;
    public Table referencedTable;
    public Column referencedColumn;
    // public CardinalityValue cardinalityValue;

    /**
     * CONSTRAINT {@code fkName} FOREIGN KEY ({@code columnName (name)})
     * REFERENCES {@code referencedEntity}({@code referencedColumn})
     */
    // public ForeignKey(String fkName, String columnName, Table referencedTable, Column referencedColumn, CardinalityValue cardinalityValue) {
    public ForeignKey(String fkName, String columnName, Table referencedTable, Column referencedColumn) {
        super(columnName);
        this.fkName = fkName;
        this.referencedTable = referencedTable;
        this.referencedColumn = referencedColumn;
        // this.cardinalityValue = cardinalityValue;
    }

    // public ForeignKey(ForeignKey other) {
    //     super(other);
    //     this.fkName = other.fkName;
    //     this.referencedTable = other.referencedTable;
    //     this.referencedColumn = other.referencedColumn;
    //     this.cardinalityValue = other.cardinalityValue;
    // }
}
