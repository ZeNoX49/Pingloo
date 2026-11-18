
package com.dbeditor.controller.view.dialogs.dialog_data;

import com.dbeditor.model.Column;
import com.dbeditor.model.Table;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Classe pour représenter une ligne des colonnes dans la TableView des dialogues
 */
public class DialogColumnEntry {

    private final Column column;
    private final SimpleStringProperty name;
    private final SimpleStringProperty type;
    private final SimpleBooleanProperty primaryKey;
    private final SimpleBooleanProperty notNull;
    private final SimpleBooleanProperty unique;
    private final SimpleBooleanProperty autoIncrement;

    public DialogColumnEntry(Column column) {
        this.column = column;
        this.name = new SimpleStringProperty(column.name);
        this.type = new SimpleStringProperty(column.type);
        this.primaryKey = new SimpleBooleanProperty(column.isPrimaryKey());
        this.notNull = new SimpleBooleanProperty(column.isNotNull);
        this.unique = new SimpleBooleanProperty(column.isUnique);
        this.autoIncrement = new SimpleBooleanProperty(column.isAutoIncrementing);
    }

    public void setName(String value) { this.name.set(value); this.column.name = value; }
    public SimpleStringProperty nameProperty() { return this.name; }

    public void setType(String value) { this.type.set(value); this.column.type = value; }
    public SimpleStringProperty typeProperty() { return this.type; }

    public void setPrimaryKey(boolean value) {
        this.primaryKey.set(value);
        this.column.setPrimaryKey(value);
        if (value) {
            this.setNotNull(true);
            this.setUnique(true);
        }
    }
    public SimpleBooleanProperty primaryKeyProperty() { return this.primaryKey; }

    public void setNotNull(boolean value) { this.notNull.set(value); this.column.isNotNull = value; }
    public SimpleBooleanProperty notNullProperty() { return this.notNull; }

    public void setUnique(boolean value) { this.unique.set(value); this.column.isUnique = value; }
    public SimpleBooleanProperty uniqueProperty() { return this.unique; }

    public void setAutoIncrement(boolean value) { this.autoIncrement.set(value); this.column.isAutoIncrementing = value; }
    public SimpleBooleanProperty autoIncrementProperty() { return this.autoIncrement; }

    // pour la validation
    public String getName() { return this.name.get(); }
    public boolean isPrimaryKey() { return this.primaryKey.get(); }

    public void removeFromTable(Table table) {
        table.removeAttribute(this.column);
    }

}