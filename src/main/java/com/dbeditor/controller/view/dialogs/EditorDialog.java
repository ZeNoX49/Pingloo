package com.dbeditor.controller.view.dialogs;

import java.util.Collection;

import com.dbeditor.manager.Singleton;
import com.dbeditor.manager.ThemeManager;
import com.dbeditor.model.Table;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public abstract class EditorDialog {
    
    private static final ThemeManager T_M = Singleton.getInstance(ThemeManager.class);

    @FXML private VBox root;
    @FXML protected Label lName;
    @FXML protected TextField tfName;
    @FXML protected HBox hboxBuilder;
    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;
    
    private final Stage stage;
    private boolean confirmed = false;

    protected final Collection<String> existingNames;
    protected final Table resultTable;

    /**
     * @param table la table à modifier, null pour créer une nouvelle
     */
    public EditorDialog(Stage stage, Table table, Collection<String> existingNames) {
        this.stage = stage;
        this.existingNames = existingNames;

        if (table == null) {
            this.resultTable = new Table("");
        } else {
            this.resultTable = table;
            this.existingNames.remove(table.name);
        }
    }

    protected void initialization() {
        this.tfName.setText(this.resultTable.name);

        // TODO: style
        this.root.setStyle("-fx-background-color: " + T_M.getCurrentTheme().getBackgroundColor());
        this.hboxBuilder.setStyle(this.hboxBuilder.getStyle());
        this.lName.setStyle("-fx-text-fill: " + T_M.getCurrentTheme().getTextColor() + ";");
        this.tfName.setStyle(this.tfName.getStyle());
        this.btnCancel.setStyle(this.btnCancel.getStyle());
        this.btnConfirm.setStyle(this.btnConfirm.getStyle());

        this.btnCancel.setOnAction(e -> {
            this.confirmed = false;
            this.stage.close();
        });
        this.btnConfirm.setOnAction(e -> {
            if (this.isValid()) {
                this.buildTable();
                this.confirmed = true;
                this.stage.close();
            }
        });
    }

    /**
     * Affiche le dialogue et attend la fermeture
     */
    public void showAndWait() {
        this.stage.showAndWait();
    }

    /**
     * Retourne true si l'utilisateur a confirmé
     */
    public boolean isConfirmed() {
        return this.confirmed;
    }

    /**
     * Valide et sauvegarde les données
     */
    protected abstract boolean isValid();

    /**
     * retourne la table créée / modifiée
     */
    public Table getResultTable() {
        return this.resultTable;
    }

    protected abstract void buildTable();
    
}