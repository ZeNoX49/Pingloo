package com.dbeditor.model;

import com.dbeditor.model.mcd.CardinalityValue;

/**
 * CONSTRAINT {@code fkName} FOREIGN KEY ({@code name}) REFERENCES {@code referencedEntity}({@code referencedColumn}),
 */
public class ForeignKey extends Attribut {
    public String fkName;
    public String referencedTable;
    public String referencedColumn;
    public CardinalityValue cardinalityValue;

    /**
     * CONSTRAINT {@code fkName} FOREIGN KEY ({@code columnName (name)}) REFERENCES {@code referencedEntity}({@code referencedColumn}),
     */
    public ForeignKey(String fkName, String columnName, String referencedTable, String referencedColumn, CardinalityValue cardinalityValue) {
        super(columnName);
        this.fkName = fkName;
        this.referencedTable = referencedTable;
        this.referencedColumn = referencedColumn;
        this.cardinalityValue = cardinalityValue;
    }

    public ForeignKey(ForeignKey other) {
        super(other);
        this.fkName = other.fkName;
        this.referencedTable = other.referencedTable;
        this.referencedColumn = other.referencedColumn;
        this.cardinalityValue = other.cardinalityValue;
    }
}
