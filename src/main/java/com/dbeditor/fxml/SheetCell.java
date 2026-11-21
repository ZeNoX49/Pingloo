package com.dbeditor.fxml;

import java.lang.constant.Constable;
import java.util.Map;
import java.util.function.Function;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;

public abstract class SheetCell<T extends Constable> {

    // TODO: style
    // private static final String CELL_STYLE = "-fx-background-color: white; -fx-border-color: #d5d9de; -fx-border-width: 0 1 1 0;";
    // private static final String FIELD_STYLE = "-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 5 6 5 6;";
    // private static final String INVALID_FIELD_STYLE = "-fx-background-color: #fff0f0; -fx-border-color: #d9534f; -fx-border-width: 1; -fx-padding: 5 6 5 6;";

    public final Class<T> type;
    public T value;
    private StackPane container;

    public SheetCell(Class<T> type, T value) {
        this.type = type;
        this.value = value;
    }

    private void setContainer(Node editor, Pos alignment) {
        this.container = new StackPane(editor);
        // this.container.setAlignment(alignment);
        // this.container.setPadding(new Insets(2));
        // this.container.setMinHeight(34);
        // this.container.setMaxWidth(Double.MAX_VALUE);
        // this.container.setStyle(SheetCell.CELL_STYLE);
    }

    public StackPane getContainer() {
        return this.container;
    }

    // ******************************************************************
    //                          SheetBooleanCell                         
    // ******************************************************************
    protected static class SheetBooleanCell extends SheetCell<Boolean> {

        public final CheckBox checkBox;

        public SheetBooleanCell(Boolean value) {
            super(Boolean.class, value);

            this.checkBox = new CheckBox();
            // this.checkBox.setSelected(Boolean.TRUE.equals(value));
            // this.checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> this.value = newValue);

            super.setContainer(this.checkBox, Pos.CENTER);
        }
    }

    // ***************************************************************
    //                          SheetTextCell                         
    // ***************************************************************
    protected static class SheetTextCell<T extends Constable> extends SheetCell<T> {

        public final TextField textField;

        public SheetTextCell(Class<T> type, T value) {
            super(type, value);

            this.textField = new TextField(this.formatValue(value));

            // this.textField.setPrefColumnCount(8);
            // this.textField.setMaxWidth(Double.MAX_VALUE);
            // this.textField.setStyle(SheetCell.FIELD_STYLE);
            this.textField.setOnAction(e -> this.commit());

            this.textField.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
                if (!isFocused) {
                    this.commit();
                }
            });

            super.setContainer(this.textField, Pos.CENTER_LEFT);
        }

        private String formatValue(T value) {
            return value == null ? "" : String.valueOf(value);
        }

        private void commit() {
            try {
                T parsed = this.parseValue();

                this.value = parsed;

                this.textField.setText(this.formatValue(parsed));
                // TODO: style -> this.textField.setStyle(Sheet.FIELD_STYLE);
                this.textField.setTooltip(null);
            } catch (IllegalArgumentException exception) {
                // Keep the previous valid value.
                // TODO: style -> this.textField.setStyle(SheetCell.INVALID_FIELD_STYLE);
                this.textField.setTooltip(new Tooltip("Invalid value: " + exception.getMessage()));
            }
        }

        private static final Map<Class<?>, Function<String, ? extends Constable>> PARSERS = Map.of(
                Byte.class, Byte::valueOf,
                Short.class, Short::valueOf,
                Integer.class, Integer::valueOf,
                Long.class, Long::valueOf,
                Float.class, Float::valueOf,
                Double.class, Double::valueOf
        );

        private T parseValue() {
            String val = this.textField.getText().trim();

            if (val.isEmpty()) {
                return null;
            }

            if (this.type == Character.class) {
                if (val.length() != 1) {
                    throw new IllegalArgumentException("Exactly one character is required.");
                }
                return this.type.cast(val.charAt(0));
            }

            Function<String, ? extends Constable> parser = SheetTextCell.PARSERS.get(this.type);

            if (parser == null) {
                throw new IllegalArgumentException("Unsupported type: " + this.type.getSimpleName());
            }

            return this.type.cast(parser.apply(val));
        }
    }

    // ********************************************************************
    //                          SheetSelectionCell                         
    // ********************************************************************
    protected static class SheetSelectionCell extends SheetCell<String> {

        public final ComboBox<String> comboBox;

        public SheetSelectionCell(String... values) {
            super(String.class, values[0]);

            this.comboBox = new ComboBox<>();
            this.comboBox.setItems(FXCollections.observableArrayList(values));

            super.setContainer(this.comboBox, Pos.CENTER_LEFT);
        }
    }

}
