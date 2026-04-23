package com.dbeditor.model;

/**
 * CONSTRAINT {@code fkName} FOREIGN KEY ({@code columnName}) REFERENCES {@code referencedEntity}({@code referencedColumn}),
 */
public class ForeignKey {
    public String fkName;
    public String columnName;
    public String referencedEntity;
    public String referencedColumn;
    public CardinalityValue cardinalityValue;

    /**
     * CONSTRAINT {@code fkName} FOREIGN KEY ({@code columnName}) REFERENCES {@code referencedEntity}({@code referencedColumn}),
     */
    public ForeignKey(String fkName, String columnName, String referencedEntity, String referencedColumn, CardinalityValue cardinalityValue) {
        this.fkName = fkName;
        this.columnName = columnName;
        this.referencedEntity = referencedEntity;
        this.referencedColumn = referencedColumn;
        this.cardinalityValue = cardinalityValue;
    }

    public ForeignKey(ForeignKey other) {
        this.fkName = other.fkName;
        this.columnName = other.columnName;
        this.referencedEntity = other.referencedEntity;
        this.referencedColumn = other.referencedColumn;
        this.cardinalityValue = other.cardinalityValue;
    }
}
