package com.dbeditor.controller.view;

import java.io.IOException;

import com.dbeditor.MainApp;
import com.dbeditor.controller.CanvasController;
import com.dbeditor.controller.TableController;
import com.dbeditor.controller.TableController.TableType;
import com.dbeditor.controller.ViewType;
import com.dbeditor.controller.view.dialogs.TableEditorDialog;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;
import com.dbeditor.sql.DbType;
import com.dbeditor.util.ThemeManager;

import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Pair;

public class MldController extends ModelView {
    private static final ThemeManager T_M = ThemeManager.getInstance();

    @Override
    public ViewType getViewType() {
        return ViewType.MLD;
    }

    @Override
    public void open(DatabaseSchema schema) throws IOException {
        if (schema == null) return;

        // supprime tous les nodes sauf selectionRect
        super.getGroup().getChildren().removeIf(node -> node != super.getLasso().rect);

        super.getTableNodes().clear();
        super.getConnectionLines().clear();

        this.createTableNodes(schema);
        this.drawConnections();

        if (super.getLasso() != null) {
            super.getLasso().rect.toFront();
        }
    }

    @Override
    public void updateType(DbType type) {
        // TODO
    }

    /**
     * Permet de créer le visuel des tables à partir d'un DatabaseSchema
     * @param schema
     */
    private void createTableNodes(DatabaseSchema schema) throws IOException {
        int col = 0, row = 0;
        int cols = (int) Math.ceil(Math.sqrt(schema.getTables().size()));

        for (Table table : schema.getTables()) {
            double x = col * 250 + 50;
            double y = row * 200 + 50;
            this.createTableNode(table, x, y);

            col++;
            if (col >= cols) {
                col = 0;
                row++;
            }
        }

        // s'assure que le rectangle de séléction est devant
        if (super.getLasso() != null) {
            super.getLasso().rect.toFront();
        }
    }

    /**
     * Crée un node de table à une position donnée
     * @param table la table à créer
     * @param x position X
     * @param y position Y
     * @return le contrôleur de la table créée
     */
    private void createTableNode(Table table, double x, double y) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/table.fxml"));
        AnchorPane tcPane = loader.load();
        TableController tcController = loader.getController();
        tcController.createTableNode(table, TableType.Table);

        super.getTableNodes().put(tcController.getTable().name, tcController);

        // gérer la sélection d'un table lorsqu'elle est cliquée
        tcController.setOnSelect((tc, e) -> super.handleSelection(tc, e));

        // Ajouter le menu contextuel avec clic droit
        tcPane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                // si double clique gauche -> on modifie la table
                if (e.getClickCount() == 2) {
                    this.editTable(tcController);
                    e.consume();
                }
            }
        });

        // attache le node pour le multidrag
        super.getMultiDrag().attach(tcController);

        // position initiale
        tcPane.setLayoutX(x);
        tcPane.setLayoutY(y);

        super.getGroup().getChildren().add(tcPane);
    }

    /**
     * Tracer tout les liens entre les tables
     */
    private void drawConnections() {
        // Supprimer les anciennes lignes
        super.getConnectionLines().forEach(pair -> super.getGroup().getChildren().remove(pair.getKey()));
        super.getConnectionLines().clear();

        for (TableController fromNode : super.getTableNodes().values()) {
            Table fromTable = fromNode.getTable();
            for (ForeignKey fk : fromTable.getForeignKeys()) {
                TableController toNode = super.getTableNodes().get(fk.referencedTable);
                if (toNode != null) {
                    this.drawConnection(fromNode, toNode);
                }
            }
        }
    }

    /**
     * Permet de tracer un lien entre 2 tables
     * @param from -> table 1
     * @param to -> table 2
     */
    private void drawConnection(TableController from, TableController to) {
        if (from == null || to == null) return;
        
        double fromX = from.getRoot().getLayoutX() + from.getRoot().getWidth() / 2;
        double fromY = from.getRoot().getLayoutY() + from.getRoot().getHeight() / 2;
        double toX = to.getRoot().getLayoutX() + to.getRoot().getWidth() / 2;
        double toY = to.getRoot().getLayoutY() + to.getRoot().getHeight() / 2;

        Line line = new Line(fromX, fromY, toX, toY);
        line.setStroke(Color.web(T_M.getTheme().getSecondaryTextColor()));
        line.setStrokeWidth(2);
        line.getStrokeDashArray().addAll(5.0, 5.0);

        // ajoute la ligne derrière le node
        super.getGroup().getChildren().add(0, line);
        super.getConnectionLines().add(new Pair<>(line, null));
        
        // bind la ligne aux tables
        line.startXProperty().bind(from.getRoot().layoutXProperty().add(from.getRoot().widthProperty().divide(2)));
        line.startYProperty().bind(from.getRoot().layoutYProperty().add(from.getRoot().heightProperty().divide(2)));
        line.endXProperty().bind(to.getRoot().layoutXProperty().add(to.getRoot().widthProperty().divide(2)));
        line.endYProperty().bind(to.getRoot().layoutYProperty().add(to.getRoot().heightProperty().divide(2)));
    }

    /**
     * Modifie une table existante
     * @param tableController le contrôleur de la table à modifier
     */
    private void editTable(TableController tableController) {
        DatabaseSchema schema = MainApp.schema;
        if (schema == null) return;

        Table oldTable = tableController.getTable();
        String oldName = oldTable.name;

        // Ouvrir le dialogue avec les données existantes
        TableEditorDialog dialog = new TableEditorDialog(oldTable);
        dialog.showAndWait();

        if (dialog.isConfirmed()) {
            Table modifiedTable = dialog.getResultTable();
            String newName = modifiedTable.name;

            // Si le nom a changé, vérifier qu'il n'existe pas déjà
            if (!oldName.equals(newName) && schema.tables.get(newName) != null) {
                CanvasController.showWarningAlert("Erreur", "Une table avec ce nom existe déjà.");
                return;
            }

            // try {
            //     // Supprimer l'ancienne table du schéma
            //     schema.removeTable(oldName);
                
            //     // Ajouter la table modifiée
            //     schema.addTable(modifiedTable);

            //     // Recréer tout le visuel (pour simplifier)
            //     // this.open(schema);

            // } catch (IOException e) {
            //     e.printStackTrace();
            //     CanvasController.showWarningAlert("Erreur", "Impossible de mettre à jour la table.");
            // }
        }
    }
}