package com.dbeditor.controller.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.dbeditor.MainApp;
import com.dbeditor.controller.ViewType;
import com.dbeditor.model.Column;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;
import com.dbeditor.util.DbManager;

import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class DfController extends TextView {
    private final static DbManager D_M = DbManager.getInstance();

    @Override
    public ViewType getViewType() {
        return ViewType.DF;
    }


    private StackPane stackPane;
    private SplitPane splitPane;
    private StackPane tvStackPane;
    private TextArea tvTextArea;
    private StackPane otherStackPane;
    private TextArea otherTextArea;

    @Override
    public void initialization(ToolBar toolbar) {
        super.initialization(toolbar);

        // df normal
        this.tvStackPane = (StackPane) super.getRoot();
        this.tvTextArea = super.getTextArea();
        
        // df export
        this.otherStackPane = new StackPane();
        this.otherTextArea = new TextArea();
        this.otherTextArea.setMinSize(0, 0);
        this.otherTextArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.otherStackPane.getChildren().add(this.otherTextArea);

        // TODO: mettre une splitpane avec 2 textarea
        this.stackPane = new StackPane();
        this.splitPane = new SplitPane();
        this.splitPane.setOrientation(Orientation.HORIZONTAL);
        this.splitPane.setMinSize(0, 0);
        this.splitPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.stackPane.getChildren().add(this.splitPane);

        this.splitPane.getItems().addAll(this.tvStackPane, this.otherStackPane);
    }

    @Override
    public void open(DatabaseSchema dbS) throws IOException {
        List<Table> tablesSorted = D_M.sortTables(dbS.getTables());


        this.doTextNormal(tablesSorted);
        this.doTextExport(tablesSorted);

        this.updateStyle();
    }

    @Override
    public void updateType(DbType type) {
        // TODO
    }

    private void doTextNormal(List<Table> tablesSorted) {
        StringBuilder text = new StringBuilder();

        for (Table t : tablesSorted) {
            List<String> pk = new ArrayList<>();
            List<String> col = new ArrayList<>();
            List<String> fk = new ArrayList<>();

            for (Column c : t.getColumns()) {
                if (c.isPrimaryKey) {
                    pk.add(c.name);
                } else {
                    col.add(c.name);
                }
            }

            for (ForeignKey f : t.getForeignKeys()) {
                fk.add(f.columnName);
            }

            text.append(String.join(", ", pk));
            text.append(" -> ");

            List<String> rightSide = new ArrayList<>();
            rightSide.addAll(col);
            rightSide.addAll(fk);

            text.append(String.join(", ", rightSide));
            text.append("\n");
        }

        this.tvTextArea.setText(text.toString());
    }

    private void doTextExport(List<Table> tablesSorted) {
        StringBuilder text = new StringBuilder();

        for (Table t : tablesSorted) {
            List<String> pk = new ArrayList<>();
            List<String> col = new ArrayList<>();
            List<String> fk = new ArrayList<>();

            for (Column c : t.getColumns()) {
                if (c.isPrimaryKey) {
                    pk.add(c.name);
                } else {
                    col.add(c.name);
                }
            }

            for (ForeignKey f : t.getForeignKeys()) {
                fk.add("{" + f.referencedTable + "}" + f.columnName);
            }

            text.append("[").append(t.name).append("] ");
            text.append(String.join(", ", pk));
            text.append(" -> ");

            List<String> rightSide = new ArrayList<>();
            rightSide.addAll(col);
            rightSide.addAll(fk);

            text.append(String.join(", ", rightSide));
            text.append("\n");
        }

        this.otherTextArea.setText(text.toString());
    }

    @Override
    public void updateStyle() {
        // TODO
    }

    @Override
    public Pane getRoot() {
        return this.stackPane;
    }
    
}