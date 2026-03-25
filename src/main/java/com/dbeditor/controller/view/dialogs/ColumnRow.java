package com.dbeditor.controller.view.dialogs;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Classe pour représenter une ligne de colonne dans la TableView
 */
public class ColumnRow {
    private final SimpleStringProperty name;
    private final SimpleStringProperty type;
    private final SimpleBooleanProperty primaryKey;
    private final SimpleBooleanProperty notNull;
    private final SimpleBooleanProperty unique;
    private final SimpleBooleanProperty autoIncrement;

    public ColumnRow(String name, String type, boolean pk, boolean nn, boolean uq, boolean ai) {
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.primaryKey = new SimpleBooleanProperty(pk);
        this.notNull = new SimpleBooleanProperty(nn);
        this.unique = new SimpleBooleanProperty(uq);
        this.autoIncrement = new SimpleBooleanProperty(ai);
    }

    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }
    public SimpleStringProperty nameProperty() { return name; }

    public String getType() { return type.get(); }
    public void setType(String value) { type.set(value); }
    public SimpleStringProperty typeProperty() { return type; }

    public boolean isPrimaryKey() { return primaryKey.get(); }
    public void setPrimaryKey(boolean value) { primaryKey.set(value); }
    public SimpleBooleanProperty primaryKeyProperty() { return primaryKey; }

    public boolean isNotNull() { return notNull.get(); }
    public void setNotNull(boolean value) { notNull.set(value); }
    public SimpleBooleanProperty notNullProperty() { return notNull; }

    public boolean isUnique() { return unique.get(); }
    public void setUnique(boolean value) { unique.set(value); }
    public SimpleBooleanProperty uniqueProperty() { return unique; }

    public boolean isAutoIncrement() { return autoIncrement.get(); }
    public void setAutoIncrement(boolean value) { autoIncrement.set(value); }
    public SimpleBooleanProperty autoIncrementProperty() { return autoIncrement; }
}