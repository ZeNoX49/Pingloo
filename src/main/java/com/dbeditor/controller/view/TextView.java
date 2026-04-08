package com.dbeditor.controller.view;

import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public abstract class TextView extends View {
    // private static final ThemeManager T_M = ThemeManager.getInstance();

    protected StackPane spPane;
    protected TextArea textArea;

    @Override
    public void initialization(ToolBar toolbar) {
        this.spPane = new StackPane();
        this.textArea = new TextArea();
        this.textArea.setMinSize(0, 0);
        this.textArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.spPane.getChildren().add(this.textArea);

        this.updateStyle();
    }

    @Override
    public void updateStyle() {
        this.textArea.setStyle("");
    }

    @Override
    public Pane getRoot() {
        return this.spPane;
    }

    protected TextArea getTextArea() { return this.textArea; }
}