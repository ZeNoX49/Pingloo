package com.dbeditor.controller.view;

import java.io.IOException;
import java.util.List;

import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.util.DbManager;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class DfController extends View {
    private static final DbManager D_M = DbManager.getInstance();

    @FXML private BorderPane root;
    @FXML private ToolBar toolbar;
    @FXML private ComboBox<String> cb;
    @FXML private Pane pane;
    @FXML private TextArea textArea;

    // @FXML
    // void initialize() {
    //     super.createSplit(this.pane);
    //     super.setupCombobowView(this.cb, View.DF);

    //     this.updateStyle();
    // }

    @Override
    public void updateStyle() {
        // TODO
    }

    @Override
    public void open(DatabaseSchema dbS) throws IOException {
        this.setupText();
    }

    @Override
    public void onChange() {
        this.setupText();
    }

    private void setupText() {
        // String text = "";
        // List<Table> tables = D_M.sortTables(dbS.getTables().values());

        // for(Table t : tables) {
        //     // 1 - trier
        //     List<String> pk = new ArrayList<>();
        //     List<String> col = new ArrayList<>();
        //     List<ForeignKey> fk = t.getForeignKeys();

        //     for(Column c : t.getColumns()) {
        //         if(c.isPrimaryKey()) {
        //             pk.add(c.getName());
        //         } else {
        //             col.add(c.getName());
        //         }
        //     }

        //     // 2 - écrire
        //     text += "[" + t.getName() + "] ";
        //     for(int ipk = 0; ipk < pk.size(); ipk++) {
        //         text += pk.get(ipk);
        //         text += (ipk == pk.size() - 1) ? " -> " : ", ";
        //     }
        //     for(int ic = 0; ic < col.size(); ic++) {
        //         text += col.get(ic);
        //         text += (ic == col.size() - 1) ? "" : ", ";
        //     }
        //     for(ForeignKey f : fk) {
        //         text += ", {" + f.getReferencedTable() + "}" + f.getColumnName();
        //     }
        //     text += "\n";
        // }

        // this.textArea.setText(text);
        // this.updateStyle();
    }
}