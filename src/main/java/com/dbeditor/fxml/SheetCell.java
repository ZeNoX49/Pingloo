package com.dbeditor.fxml;

import java.lang.constant.Constable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public abstract class SheetCell<T extends Constable> {

    // TODO: style
    // private static final String CELL_STYLE = "-fx-background-color: white; -fx-border-color: #d5d9de; -fx-border-width: 0 1 1 0;";
    // private static final String VALID_STYLE = "-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 5 6 5 6;";
    // private static final String INVALID_STYLE = "-fx-background-color: #fff0f0; -fx-border-color: #d9534f; -fx-border-width: 1; -fx-padding: 5 6 5 6;";

    protected final Class<T> type;
    private StackPane container;
    private boolean valid;

    private final List<Consumer<T>> listeners = new ArrayList<>();
    private final List<Function<T, Boolean>> validators = new ArrayList<>();

    protected SheetCell(Class<T> type) {
        this.type = type;
    }

    private void setContainer(Node editor, Pos alignment) {
        this.container = new StackPane(editor);
        // this.container.setAlignment(alignment);
        // this.container.setPadding(new Insets(2));
        // this.container.setMinHeight(34);
        // this.container.setMaxWidth(Double.MAX_VALUE);
        // this.container.setStyle(SheetCell.CELL_STYLE);
    }

    protected StackPane getContainer() {
        return this.container;
    }

    protected abstract T getValue();

    protected abstract void setValid(boolean valid);

    protected boolean isValid() {
        return this.valid;
    }

    /**
     * @param defaultValue {@code null} pour ne pas changer la valeur actuelle
     */
    protected abstract void setEnabled(boolean enabled, T defaultValue);

    protected void addListener(Consumer<T> listener) {
        this.listeners.add(listener);
    }

    protected void addValidator(Function<T, Boolean> validator) {
        this.validators.add(validator);
    }

    private void notifyAll(T value) {
        for (Consumer<T> listener : this.listeners) {
            listener.accept(value);
        }

        for (Function<T, Boolean> validator : this.validators) {
            if (!validator.apply(value)) {
                this.setValid(false);
                return;
            }
        }
        this.setValid(true);
    }

    private static final Map<Class<?>, Function<String, ? extends Constable>> PARSERS = Map.of(
            Byte.class, Byte::valueOf,
            Short.class, Short::valueOf,
            Integer.class, Integer::valueOf,
            Long.class, Long::valueOf,
            Float.class, Float::valueOf,
            Double.class, Double::valueOf
    );

    protected T parseValue(String value) {
        String val = value.trim();

        if (val.isEmpty()) {
            return null;
        }

        if (this.type == Character.class) {
            if (val.length() != 1) {
                throw new IllegalArgumentException("Exactly one character is required.");
            }
            return this.type.cast(val.charAt(0));
        }

        Function<String, ? extends Constable> parser = SheetCell.PARSERS.get(this.type);

        if (parser == null) {
            throw new IllegalArgumentException("Unsupported type: " + this.type.getSimpleName());
        }

        return this.type.cast(parser.apply(val));
    }

    // ******************************************************************
    //                          SheetBooleanCell                         
    // ******************************************************************
    protected static class SheetBooleanCell extends SheetCell<Boolean> {

        private final CheckBox checkBox;

        protected SheetBooleanCell(Boolean value) {
            super(Boolean.class);
            super.valid = true;

            this.checkBox = new CheckBox();
            this.checkBox.setSelected(Boolean.TRUE.equals(value));
            this.checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> super.notifyAll(newValue));

            super.setContainer(this.checkBox, Pos.CENTER);
        }

        @Override 
        protected Boolean getValue() {
            return this.checkBox.isSelected();
        }

        @Override
        protected void setValid(boolean valid) {}
        
        @Override
        protected void setEnabled(boolean enabled, Boolean defaultValue) {
            this.checkBox.setDisable(!enabled);
            if (defaultValue != null) {
                this.checkBox.setSelected(defaultValue);
            }
        }
    }

    // ***************************************************************
    //                          SheetTextCell                         
    // ***************************************************************
    protected static class SheetTextCell<T extends Constable> extends SheetCell<T> {

        private final TextField textField;

        protected SheetTextCell(Class<T> type, T value) {
            super(type);

            this.textField = new TextField();

            // this.textField.setPrefColumnCount(8);
            // this.textField.setMaxWidth(Double.MAX_VALUE);
            // this.textField.setStyle(SheetCell.FIELD_STYLE);
            this.textField.setOnAction(e -> this.commit());

            this.textField.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
                if (!isFocused) {
                    this.commit();
                }
            });

            this.textField.setText(this.formatValue(value));

            super.setContainer(this.textField, Pos.CENTER_LEFT);
        }

        private String formatValue(T value) {
            return value == null ? "" : String.valueOf(value);
        }

        private void commit() {
            T parsed = super.parseValue(this.textField.getText());
            this.textField.setText(this.formatValue(parsed));
            super.notifyAll(parsed);
        }

        @Override 
        protected T getValue() {
            return super.parseValue(this.textField.getText());
        }

        @Override
        protected void setValid(boolean valid) {
            super.valid = valid;
            // TODO: style -> this.textField.setStyle(valid ? SheetCell.VALID_STYLE : INVALID_STYLE);
        }

        @Override
        protected void setEnabled(boolean enabled, T defaultValue) {
            this.textField.setDisable(!enabled);
            if (defaultValue != null) {
                this.textField.setText(this.formatValue(defaultValue));
            }
        }
    }

    // ********************************************************************
    //                          SheetSelectionCell                         
    // ********************************************************************
    protected static class SheetSelectionCell<T extends Constable> extends SheetCell<T> {

        private final ComboBox<String> comboBox;

        protected SheetSelectionCell(Class<T> type, String... values) {
            super(type);

            this.comboBox = new ComboBox<>();
            this.comboBox.setItems(FXCollections.observableArrayList(values));

            this.comboBox.valueProperty().addListener(
                (obs, oldValue, newValue) -> {
                    super.notifyAll(super.parseValue(newValue));
                }
            );

            super.setContainer(this.comboBox, Pos.CENTER);
        }

        @Override 
        protected T getValue() {
            return super.parseValue(this.comboBox.getValue());
        }

        @Override
        protected void setValid(boolean valid) {
            super.valid = valid;
            // TODO: style -> this.comboBox.setStyle(valid ? SheetCell.VALID_STYLE : INVALID_STYLE);
        }

        @Override
        protected void setEnabled(boolean enabled, T defaultValue) {
            this.comboBox.setDisable(!enabled);
            if (defaultValue != null) {
                this.comboBox.setValue(defaultValue.toString());
            }
        }
    }

}
