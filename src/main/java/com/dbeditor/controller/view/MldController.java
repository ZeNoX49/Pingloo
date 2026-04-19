package com.dbeditor.controller.view;

import java.io.IOException;

import com.dbeditor.MainApp;
import com.dbeditor.controller.CanvasController;
import com.dbeditor.controller.TableController;
import com.dbeditor.controller.TableController.TableType;
import com.dbeditor.controller.ViewType;
import com.dbeditor.controller.modifier.Drag;
import com.dbeditor.controller.view.dialogs.TableEditorDialog;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;
import com.dbeditor.util.ThemeManager;

import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class MldController extends ModelView {
    private static final ThemeManager T_M = ThemeManager.getInstance();

    @Override
    public ViewType getViewType() {
        return ViewType.MLD;
    }

    @Override
    public void open() {
        // supprime tous les nodes sauf selectionRect
        super.group.getChildren().removeIf(node -> node != super.lasso.rect);

        super.tableNodes.clear();
        super.connectionLines.clear();

        this.createTableNodes();
        this.drawConnections();

        super.lasso.rect.toFront();

        super.updateStyle();
    }

    /**
     * Permet de créer le visuel des tables à partir d'un DatabaseSchema
     */
    private void createTableNodes() {
        for (Table table : MainApp.schema.getTables()) {
            this.createTableNode(table);
        }
    }

    /**
     * Crée un node de table
     * @param table la table à créer
     */
    private void createTableNode(Table table) {
        AnchorPane tcPane;
        TableController tcController;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/table.fxml"));
            tcPane = loader.load();
            tcController = loader.getController();
        } catch (IOException e) { throw new Error("Une erreur est survenue lors de la création du visuel"); }

        tcController.createTableController(table, TableType.Table);

        super.tableNodes.put(table.name, tcController);

        // gérer la sélection d'un table lorsqu'elle est cliquée
        tcController.setOnSelect((tc, e) -> super.handleSelection(tc, e));

        // Ajouter le menu contextuel avec clic droit
        tcPane.setOnMouseClicked(e -> {
            // si double clique gauche -> on modifie la table
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                this.editTable(tcController);
                e.consume();
            }
        });

        // mettre a jour la position lors d'un déplacement
        tcPane.layoutXProperty().addListener((a, b, c) -> table.setPosition(tcPane.getLayoutX(), table.getPosY()));
        tcPane.layoutYProperty().addListener((a, b, c) -> table.setPosition(table.getPosX(), tcPane.getLayoutY()));

        // attache le node pour le multidrag
        super.multiDrag.attach(tcController);

        super.group.getChildren().add(tcPane);
    }

    /**
     * Tracer tout les liens entre les tables
     */
    private void drawConnections() {
        // Supprimer les anciennes lignes
        super.connectionLines.forEach(connection -> super.group.getChildren().remove(connection.line));
        super.connectionLines.clear();

        for (Drag d : super.tableNodes.values()) {
            TableController fromNode = super.getTableController(d);
            if (fromNode == null) continue;

            Table fromTable = fromNode.getTable();
            for (ForeignKey fk : fromTable.getForeignKeys()) {
                TableController toNode = super.getTableController(super.tableNodes.get(fk.referencedTable));
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
        
        // bind la ligne aux tables
        line.startXProperty().bind(from.getRoot().layoutXProperty().add(from.getRoot().widthProperty().divide(2)));
        line.startYProperty().bind(from.getRoot().layoutYProperty().add(from.getRoot().heightProperty().divide(2)));
        line.endXProperty().bind(to.getRoot().layoutXProperty().add(to.getRoot().widthProperty().divide(2)));
        line.endYProperty().bind(to.getRoot().layoutYProperty().add(to.getRoot().heightProperty().divide(2)));
    
        // ajoute la ligne derrière le node
        super.group.getChildren().add(0, line);
        super.connectionLines.add(new Connection(from.getTable().name, to.getTable().name, line, null));
    }

    /**
     * Modifie une table existante
     */
    private void editTable(TableController tc) {
        Table oldTable = tc.getTable();
        String oldName = oldTable.name;

        TableEditorDialog dialog = new TableEditorDialog(oldTable);
        dialog.showAndWait();
        if (!dialog.isConfirmed()) return;

        Table modifiedTable = dialog.getResultTable();
        String newName = modifiedTable.name;
        
        // Si le nom a changé, vérifier qu'il n'existe pas déjà
        if (!oldName.equals(newName) && MainApp.schema.tables.get(newName) != null) {
            CanvasController.showWarningAlert("Erreur", "Une table avec ce nom existe déjà.");
            return;
        }

        // TODO
        // // Mettre à jour dans le ConceptualSchema
        // this.conceptualSchema.updateEntity(oldName, modifiedTable);

        // // Mise à jour du schema global
        // MainApp.schema.tables.remove(oldName);
        // MainApp.schema.addTable(modifiedTable);

        // // Supprime l'ancien node visuel
        // super.group.getChildren().remove(tc.getRoot());
        // super.tableNodes.remove(oldName);

        // // Supprime les anciens liens liés à cette entité
        // Iterator<Connection> it = super.connectionLines.iterator();
        // while (it.hasNext()) {
        //     Connection connection = it.next();

        //     if (connection.firstTable.equals(oldName)) {
        //         super.group.getChildren().removeAll(connection.line, connection.label);
        //         it.remove();
        //     }
        // }

        // // Recrée le node avec le nouveau nom
        // this.createTableNode(modifiedTable, TableType.Entity);

        // TableController newTc = super.tableNodes.get(newName);

        // // Redessine uniquement les liens de cette entité
        // for (Entry<String, List<Pair<Table, CardinalityValue>>> entry : this.conceptualSchema.getLinks().entrySet()) {
        //     String assocName = entry.getKey();
        //     TableController assocTc = super.tableNodes.get(assocName);
        //     if (assocTc == null) continue;

        //     for (Pair<Table, CardinalityValue> p : entry.getValue()) {
        //         if (p.getKey().name.equals(newName)) {
        //             this.drawConnection(newTc, assocTc, p.getValue());
        //             break;
        //         }
        //     }
        // }
    }
}