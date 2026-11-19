package com.dbeditor.controller.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.dbeditor.controller.CanvasController;
import com.dbeditor.controller.TableController;
import com.dbeditor.controller.TableController.TableType;
import com.dbeditor.controller.ViewType;
import com.dbeditor.controller.view.dialogs.AssociationEditorController;
import com.dbeditor.controller.view.dialogs.EntityEditorController;
import com.dbeditor.manager.Singleton;
import com.dbeditor.manager.ThemeManager;
import com.dbeditor.model.CardinalityValue;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class McdController extends ModelView {

    private static final ThemeManager T_M = Singleton.getInstance(ThemeManager.class);

    @Override
    public ViewType getViewType() {
        return ViewType.MCD;
    }

    private Button btnEntity, btnAssociation;
    private DatabaseSchema conceptualSchema;

    @Override
    public void initialization(ToolBar toolbar) {
        this.conceptualSchema = new DatabaseSchema("");

        this.btnEntity = super.createButton("Entité");
        this.btnAssociation = super.createButton("Association");
        toolbar.getItems().addAll(this.btnEntity, this.btnAssociation);

        super.initialization(toolbar);

        // TODO: peut ajouter un ctrl c / v -> List<TableController> copied -> si une seul .setPosition(e.getX(), e.getY()) | si > 1 faire en fonction de la bouding box (faire gaffe a où est le (0, 0) des tables)
        this.getRoot().sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.DELETE) {
                        this.deleteSelected();
                    } else if (e.getCode() == KeyCode.D && e.isControlDown()) {
                        this.duplicateTables();
                    }
                });
            }
        });

        this.btnEntity.setOnAction(e -> this.addEntity());
        this.btnAssociation.setOnAction(e -> this.addAssociation());
    }

    @Override
    public void open(DatabaseSchema schema) {
        // this.conceptualSchema = new ConceptualSchema();

        // // supprime tous les nodes sauf selectionRect
        // super.group.getChildren().removeIf(node -> node != super.lasso.rect);

        // // vider les structures
        // super.tableNodes.clear();
        // super.connectionLines.clear();

        // // créer les nodes à partir du MCD
        // this.createTableNodes();
        // this.drawConnections();

        // super.lasso.rect.toFront();

        // super.updateStyle();
    }

    // public void createSync() {
    //     CanvasController.schema = this.conceptualSchema.toSchema();
    // }

    // /**
    //  * Crée les nodes visuels pour les entités
    //  */
    // private void createTableNodes() {
    //     for (Table table : this.conceptualSchema.getAllEntities()) {
    //         this.createTableNode(table, TableType.Entity);
    //     }
    // }

    private void duplicateTables() {
        for (TableController tc : this.selectionModel.getSelected()) {
            Table dupli = new Table(tc.getTable());

            while (super.tableNodes.values().contains(dupli.name)) {
                dupli.name += " copy";
            }

            this.conceptualSchema.addTable(dupli);

            TableController assoTc = this.createTableNode(dupli, tc.getType());

            if (assoTc.getType() == TableType.Association) {
                List<TableController> entities = super.tableNodes.keySet().stream().filter(con -> con.getType() == TableType.Entity).toList();

                for (ForeignKey fk : assoTc.getTable().getForeignKeys()) {
                    for (TableController con : entities) {
                        if (con.getTable().equals(fk.referencedTable)) {
                            drawConnection(con, assoTc, fk.cardinalityValue);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Crée un node d'entité/association
     */
    private TableController createTableNode(Table table, TableType tabletype) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/table.fxml"));
            TableController tcController = new TableController(table, tabletype);
            loader.setController(tcController);
            AnchorPane tcPane = loader.load();

            super.tableNodes.put(tcController, table.name);

            // Gérer la sélection
            tcController.setOnSelect((e) -> super.handleSelection(tcController, e));

            // Menu contextuel
            tcPane.setOnMouseClicked(e -> {
                // si double clique gauche -> modifier la table ou l'association
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                    if (tabletype == TableType.Entity) {
                        this.editEntity(tcController);
                    } else {
                        this.editAssociation(tcController);
                    }
                    e.consume();
                }
            });

            // mettre a jour la position lors d'un déplacement
            tcPane.layoutXProperty().addListener((a, b, c) -> table.setPosition(tcPane.getLayoutX(), table.getPosY()));
            tcPane.layoutYProperty().addListener((a, b, c) -> table.setPosition(table.getPosX(), tcPane.getLayoutY()));

            // attache le node pour le multidrag
            super.multiDrag.attach(tcController);

            super.group.getChildren().add(tcPane);

            return tcController;
        } catch (IOException e) {
            System.err.println("Une erreur est survenue lors de la création du visuel");
            e.printStackTrace();
            return null;
        }
    }

    // /**
    //  * Tracer tout les liens entre les tables
    //  */
    // private void drawConnections() {
    //     // Supprimer les anciennes lignes
    //     super.connectionLines.forEach(connection -> {
    //         super.group.getChildren().remove(connection.line);
    //         super.group.getChildren().remove(connection.label);
    //     });
    //     super.connectionLines.clear();

    //     for (Table table : this.conceptualSchema.getAllAssociations()) {

    //         if (!table.isPositionned()) {
    //             // centrer l'association
    //             double sumX = 0;
    //             double sumY = 0;

    //             for (ForeignKey fk : table.getForeignKeys()) {
    //                 Table ref = this.conceptualSchema.getEntity(fk.referencedTable);
    //                 sumX += ref.getPosX();
    //                 sumY += ref.getPosY();
    //             }

    //             table.setPosition(
    //                     sumX / table.getForeignKeys().size(),
    //                     sumY / table.getForeignKeys().size()
    //             );
    //         }

    //         this.createTableNode(table, TableType.Association);
    //         TableController ac = super.tableNodes.get(table.name);

    //         for (ForeignKey fk : table.getForeignKeys()) {
    //             Table ref = this.conceptualSchema.getEntity(fk.referencedTable);
    //             TableController ec = super.tableNodes.get(ref.name);

    //             this.drawConnection(ec, ac, fk.cardinalityValue);
    //         }
    //     }
    // }

    /**
     * Permet de tracer un lien entre une entité et une association
     */
    private void drawConnection(TableController fromEntity, TableController toAsso, CardinalityValue cardinality) {
        // permet de savoir quelle TableController récupérer lors d'une modif/suppression
        if (fromEntity == null || fromEntity.getType() != TableType.Entity) {
            return;
        }
        if (toAsso == null || toAsso.getType() != TableType.Association) {
            return;
        }

        double fromX = fromEntity.getRoot().getLayoutX() + fromEntity.getRoot().getWidth() / 2;
        double fromY = fromEntity.getRoot().getLayoutY() + fromEntity.getRoot().getHeight() / 2;
        double toX = toAsso.getRoot().getLayoutX() + toAsso.getRoot().getWidth() / 2;
        double toY = toAsso.getRoot().getLayoutY() + toAsso.getRoot().getHeight() / 2;

        Line line = new Line(fromX, fromY, toX, toY);
        line.setStroke(Color.web(T_M.getCurrentTheme().getSecondaryTextColor()));
        line.setStrokeWidth(2);
        line.getStrokeDashArray().addAll(5.0, 5.0);

        // bind la ligne aux tables
        line.startXProperty().bind(fromEntity.getRoot().layoutXProperty().add(fromEntity.getRoot().widthProperty().divide(2)));
        line.startYProperty().bind(fromEntity.getRoot().layoutYProperty().add(fromEntity.getRoot().heightProperty().divide(2)));
        line.endXProperty().bind(toAsso.getRoot().layoutXProperty().add(toAsso.getRoot().widthProperty().divide(2)));
        line.endYProperty().bind(toAsso.getRoot().layoutYProperty().add(toAsso.getRoot().heightProperty().divide(2)));

        // Texte de la cardinalité
        Label cardinalityLabel = new Label(cardinality.toString());

        // Bind le Label au centre de la ligne
        cardinalityLabel.layoutXProperty().bind(
                line.startXProperty().add(line.endXProperty().subtract(line.startXProperty()).divide(2))
                        .subtract(cardinalityLabel.widthProperty().divide(2)) // centrer horizontalement
        );
        cardinalityLabel.layoutYProperty().bind(
                line.startYProperty().add(line.endYProperty().subtract(line.startYProperty()).divide(2))
                        .subtract(cardinalityLabel.heightProperty().divide(2)) // centrer verticalement
        );

        // ajoute la ligne derrière le node
        super.group.getChildren().add(0, cardinalityLabel);
        super.group.getChildren().add(0, line);
        super.connectionLines.add(new Connection(fromEntity, toAsso, line, cardinalityLabel));
    }

    public void addEntity() {
        EntityEditorController dialog = this.loadEntityEditorDialog(null, "Créer une entité");
        if (dialog == null) {
            return;
        }

        dialog.showAndWait();
        if (!dialog.isConfirmed()) {
            return;
        }

        Table table = dialog.getResultTable();

        this.conceptualSchema.addTable(table);
        this.createTableNode(table, TableType.Entity);
    }

    private void editEntity(TableController tc) {
        EntityEditorController dialog = this.loadEntityEditorDialog(tc.getTable(), "Modifier une entité");
        if (dialog == null) {
            return;
        }

        dialog.showAndWait();
        if (!dialog.isConfirmed()) {
            return;
        }

        // recharge le noeud pour avoir les modifications
        tc.reload();
        // met a jour le nom dans la map
        super.tableNodes.put(tc, dialog.getResultTable().name);
    }

    private EntityEditorController loadEntityEditorDialog(Table table, String name) {
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle(name);
            stage.setResizable(true);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialog.fxml"));
            EntityEditorController controller = new EntityEditorController(stage, table, new HashSet<>(super.tableNodes.values()));
            loader.setController(controller);
            VBox root = loader.load();

            stage.setScene(new Scene(root));

            return controller;
        } catch (IOException e) {
            System.err.println("Une erreur est survenue lors de la creation du visuel");
            e.printStackTrace();
            return null;
        }
    }

    public void addAssociation() {
        Map<String, TableController> tables = new HashMap<>();
        for (TableController tc : super.tableNodes.keySet()) {
            tables.put(tc.getTable().name, tc);
        }

        AssociationEditorController dialog = this.loadAssociationEditorDialog(null, "Créer une association", tables);
        if (dialog == null) {
            return;
        }

        dialog.showAndWait();
        if (!dialog.isConfirmed()) {
            return;
        }

        Table asso = dialog.getResultTable();

        this.conceptualSchema.addTable(asso);
        TableController assoTc = this.createTableNode(asso, TableType.Association);

        for (ForeignKey fk : asso.getForeignKeys()) {
            TableController entityTc = tables.get(fk.referencedTable.name);
            this.drawConnection(entityTc, assoTc, fk.cardinalityValue);
        }
    }

    private void editAssociation(TableController tcAsso) {
        Map<String, TableController> tables = new HashMap<>();
        for (TableController tc : super.tableNodes.keySet()) {
            tables.put(tc.getTable().name, tc);
        }

        AssociationEditorController dialog = this.loadAssociationEditorDialog(tcAsso.getTable(), "Créer une association", tables);
        if (dialog == null) {
            return;
        }

        dialog.showAndWait();
        if (!dialog.isConfirmed()) {
            return;
        }

        Table asso = dialog.getResultTable();

        // recharge le noeud pour avoir les modifications
        tcAsso.reload();
        // met a jour le nom dans la map
        super.tableNodes.put(tcAsso, asso.name);

        this.removeConnectionsInvolving(tcAsso);

        for (ForeignKey fk : asso.getForeignKeys()) {
            TableController entityTc = tables.get(fk.referencedTable.name);
            this.drawConnection(entityTc, tcAsso, fk.cardinalityValue);
        }
    }

    private AssociationEditorController loadAssociationEditorDialog(Table table, String name, Map<String, TableController> tables) {
        List<String> entitiesName = new ArrayList<>();
        for (Table t : this.conceptualSchema.getTables()) {
            if (t.getForeignKeys().isEmpty()) {
                entitiesName.add(t.name);
            }
        }

        if (entitiesName.isEmpty()) {
            CanvasController.showWarningAlert("Erreur", "Il faut au moins 1 entité pour créer une association.");
            return null;
        }

        try {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle(name);
            stage.setResizable(true);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialog.fxml"));
            AssociationEditorController controller = new AssociationEditorController(stage, table, entitiesName, new HashSet<>(super.tableNodes.values()), n -> tables.get(n).getTable());
            loader.setController(controller);
            VBox root = loader.load();

            stage.setScene(new Scene(root));

            return controller;
        } catch (IOException e) {
            System.err.println("Une erreur est survenue lors de la creation du visuel");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Supprime les entités et associations sélectionnées
     */
    public void deleteSelected() {
        List<TableController> selected = super.selectionModel.getSelected();

        String message = selected.size() == 1
                ? "Supprimer « " + selected.get(0).getTable().name + " » ?"
                : "Supprimer " + selected.size() + " éléments ?";

        if (!CanvasController.showConfirmationAlert("Confirmation", "Supprimer", message)) {
            return;
        }

        Consumer<TableController> deleteTc = (tc) -> {
            super.group.getChildren().remove(tc.getRoot());
            super.tableNodes.remove(tc);
            this.conceptualSchema.removeTable(tc.getTable());
        };

        List<TableController> selectedEntity = selected.stream().filter(tc -> tc.getType() == TableType.Entity).toList();
        List<TableController> selectedAsso = new ArrayList<>(selected.stream().filter(tc -> tc.getType() == TableType.Association).toList());
        
        for (TableController tc : selectedEntity) {
            for (Connection c : super.connectionLines) {
                TableController assoTc = c.getTableController(2);   // normalement l'association
                if (c.involves(tc) & !selectedAsso.contains(assoTc)) {
                    selectedAsso.add(assoTc);
                }
            }
            deleteTc.accept(tc);
        }

        for (TableController tc : selectedAsso) {
            this.removeConnectionsInvolving(tc);
            deleteTc.accept(tc);
        }

        super.selectionModel.clear();
    }

    private void removeConnectionsInvolving(TableController tc) {
        Iterator<Connection> it = super.connectionLines.iterator();
        while (it.hasNext()) {
            Connection c = it.next();
            if (c.involves(tc)) {
                c.removeFrom(super.group);
                it.remove();
            }
        }
    }

}
