package com.dbeditor.controller.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.dbeditor.MainApp;
import com.dbeditor.controller.CanvasController;
import com.dbeditor.controller.TableController;
import com.dbeditor.controller.TableController.TableType;
import com.dbeditor.controller.ViewType;
import com.dbeditor.controller.view.dialogs.AssociationEditorController;
import com.dbeditor.controller.view.dialogs.EntityEditorController;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;
import com.dbeditor.model.mcd.CardinalityValue;
import com.dbeditor.model.mcd.ConceptualSchema;
import com.dbeditor.util.ThemeManager;

import javafx.fxml.FXML;
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
    private static final ThemeManager T_M = ThemeManager.getInstance();
    
    @Override
    public ViewType getViewType() {
        return ViewType.MCD;
    }

    private Button btnEntity, btnAssociation;

    private ConceptualSchema conceptualSchema;
    
    @Override
    public void initialization(ToolBar toolbar) {
        this.conceptualSchema = new ConceptualSchema(MainApp.schema);

        this.btnEntity = super.createButton("Entité");
        this.btnAssociation = super.createButton("Association");
        toolbar.getItems().addAll(this.btnEntity, this.btnAssociation);

        super.initialization(toolbar);
        
        this.getRoot().sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.DELETE) {
                        this.deleteSelected();
                    }

                    else if (e.getCode() == KeyCode.D && e.isControlDown()) {
                        this.duplicateTables();
                    }
                });
            }
        });

        this.btnEntity.setOnAction(e -> this.addEntity());
        this.btnAssociation.setOnAction(e -> this.addAssociation());
    }

    private void duplicateTables() {
        for(TableController tc : this.selectionModel.getSelected()) {
            Table dupli = new Table(tc.getTable());
            if(dupli.isPositionned()) {
                dupli.setPosition(dupli.getPosX() + 10, dupli.getPosY() + 10);
            }

            while(super.tableNodes.get(dupli.name) != null) {
                dupli.name += " copy";
            }

            if(tc.getType() == TableType.Entity) {
                this.conceptualSchema.addEntity(dupli);
            } else {
                this.conceptualSchema.addAssociation(dupli);
            }

            this.createTableNode(dupli, tc.getType());
        }
    }

    @Override
    public void open() {
        this.conceptualSchema = new ConceptualSchema(MainApp.schema);

        // supprime tous les nodes sauf selectionRect
        super.group.getChildren().removeIf(node -> node != super.lasso.rect);

        // vider les structures
        super.tableNodes.clear();
        super.connectionLines.clear();

        // créer les nodes à partir du MCD
        this.createTableNodes();
        this.drawConnections();

        super.lasso.rect.toFront();

        super.updateStyle();
    }

    @Override
    public void createSync() {
        DatabaseSchema schema = new DatabaseSchema(MainApp.schema.name);

        for(Table t : this.conceptualSchema.getAllEntities()) {
            schema.addTable(t);
        }
    }

    /**
     * Crée les nodes visuels pour les entités
     */
    private void createTableNodes() {
        for (Table table : this.conceptualSchema.getAllEntities()) {
            this.createTableNode(table, TableType.Entity);
        }
    }

    /**
     * Crée un node d'entité/association
     */
    private void createTableNode(Table table, TableType tabletype) {
        AnchorPane tcPane;
        TableController tcController;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/table.fxml"));
            tcPane = loader.load();
            tcController = loader.getController();
        } catch (IOException e) { throw new Error("Une erreur est survenue lors de la création du visuel"); }

        tcController.createTableController(table, tabletype);

        super.tableNodes.put(table.name, tcController);

        // Gérer la sélection
        tcController.setOnSelect((tc, e) -> super.handleSelection((TableController) tc, e));

        // Menu contextuel
        tcPane.setOnMouseClicked(e -> {
            // si double clique gauche -> modifier la table ou l'association
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                if(tabletype == TableType.Entity) {
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
    }

    /**
     * Tracer tout les liens entre les tables
     */
    private void drawConnections() {
        // Supprimer les anciennes lignes
        super.connectionLines.forEach(connection -> {
            super.group.getChildren().remove(connection.line);
            super.group.getChildren().remove(connection.label);
        });
        super.connectionLines.clear();

        for (Table table : this.conceptualSchema.getAllAssociations()) {

            if(!table.isPositionned()) {
                // centrer l'association
                double sumX = 0;
                double sumY = 0;

                for(ForeignKey fk : table.getForeignKeys()) {
                    Table ref = this.conceptualSchema.getEntity(fk.referencedTable);
                    sumX += ref.getPosX();
                    sumY += ref.getPosY();
                }

                table.setPosition(
                    sumX / table.getForeignKeys().size(),
                    sumY / table.getForeignKeys().size()
                );
            }

            this.createTableNode(table, TableType.Association);
            TableController ac = super.tableNodes.get(table.name);

            for(ForeignKey fk : table.getForeignKeys()) {
                Table ref = this.conceptualSchema.getEntity(fk.referencedTable);
                TableController ec = super.tableNodes.get(ref.name);

                this.drawConnection(ec, ac, fk.cardinalityValue);
            }
        }
    }

    /**
     * Permet de tracer un lien entre une entité et une association
     */
    private void drawConnection(TableController fromEntity, TableController toAsso, CardinalityValue cardinality) {
        // permet de savoir quelle TableController récupérer lors d'une modif/suppression
        if (fromEntity == null || fromEntity.getType() != TableType.Entity) return;
        if (toAsso == null || toAsso.getType() != TableType.Association) return;

        double fromX = fromEntity.getRoot().getLayoutX() + fromEntity.getRoot().getWidth() / 2;
        double fromY = fromEntity.getRoot().getLayoutY() + fromEntity.getRoot().getHeight() / 2;
        double toX = toAsso.getRoot().getLayoutX() + toAsso.getRoot().getWidth() / 2;
        double toY = toAsso.getRoot().getLayoutY() + toAsso.getRoot().getHeight() / 2;

        Line line = new Line(fromX, fromY, toX, toY);
        line.setStroke(Color.web(T_M.getTheme().getSecondaryTextColor()));
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
        super.connectionLines.add(new Connection(fromEntity.getTable().name, toAsso.getTable().name, line, cardinalityLabel));
    }

    /**
     * Ajoute une nouvelle entité
     */
    @FXML
    public void addEntity() {
        EntityEditorController dialogCon = this.loadEntityEditorDialog(null, "Créer une entité");

        dialogCon.showAndWait();
        if (!dialogCon.isConfirmed()) return;
        
        Table table = dialogCon.getResultTable();
        
        if (this.conceptualSchema.nameExists(table.name)) {
            CanvasController.showWarningAlert("Erreur", "Ce nom est déja utilisé.");
            return;
        }

        this.conceptualSchema.addEntity(table);
        
        this.createTableNode(table, TableType.Entity);
    }

    /**
     * Édite une entité existante.
     */
    private void editEntity(TableController tc) {
        Table oldTable = tc.getTable();
        String oldName = oldTable.name;
    
        EntityEditorController dialogCon = this.loadEntityEditorDialog(oldTable, "Modifier une entité");

        dialogCon.showAndWait();
        if (!dialogCon.isConfirmed()) return;

        Table modifiedTable = dialogCon.getResultTable();
        String newName = modifiedTable.name;

        if (!oldName.equals(newName) && this.conceptualSchema.getEntity(newName) != null) {
            CanvasController.showWarningAlert("Erreur", "Une entité nommée « " + newName + " » existe déjà.");
            return;
        }

        // Mettre à jour dans le ConceptualSchema
        this.conceptualSchema.updateEntity(oldName, modifiedTable);

        // Mise à jour du schema global
        MainApp.schema.tables.remove(oldName);
        MainApp.schema.addTable(modifiedTable);

        // Supprime l'ancien node visuel
        super.group.getChildren().remove(tc.getRoot());
        super.tableNodes.remove(oldName);

        // Supprime les anciens liens liés à cette entité
        Iterator<Connection> it = super.connectionLines.iterator();
        while (it.hasNext()) {
            Connection connection = it.next();

            if (connection.firstTable.equals(oldName)) {
                super.group.getChildren().removeAll(connection.line, connection.label);
                it.remove();
            }
        }

        // Recrée le node avec le nouveau nom
        this.createTableNode(modifiedTable, TableType.Entity);

        TableController newTc = super.tableNodes.get(newName);

        // TODO
        // Redessine uniquement les liens de cette entité
        for (Table asso : this.conceptualSchema.getAllAssociations()) {
            TableController assocTc = super.tableNodes.get(asso.name);

            for (ForeignKey fk : asso.getForeignKeys()) {
                if (fk.referencedTable.equals(newName)) {
                    this.drawConnection(newTc, assocTc, fk.cardinalityValue);
                    break;
                }
            }
        }
    }

    private EntityEditorController loadEntityEditorDialog(Table table, String name) {
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle(name);
            stage.setResizable(true);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/entityEditor.fxml"));
            VBox root = loader.load();
            EntityEditorController dialogCon = loader.getController();
            dialogCon.setData(stage, table);

            stage.setScene(new Scene(root));

            return dialogCon;
        } catch (IOException e) {
            throw new Error("Une erreur est survenue lors de la création du visuel : \n" + e.getMessage(), e);
        }
    }

    /**
     * Ajoute une nouvelle association.
     */
    public void addAssociation() {
        List<Table> entities = this.conceptualSchema.getAllEntities();
        if (entities.isEmpty()) {
            CanvasController.showWarningAlert("Erreur", "Il faut au moins 1 entité pour créer une association.");
            return;
        }

        AssociationEditorController dialog = this.loadAssociationEditorDialog(entities, null, "Créer une association");
        dialog.showAndWait();
        if (!dialog.isConfirmed()) return;

        Table asso = dialog.getResultTable();
        String name = asso.name;

        if (this.conceptualSchema.nameExists(name)) {
            CanvasController.showWarningAlert("Erreur", "Ce nom est déja utilisé.");
            return;
        }

        this.conceptualSchema.addAssociation(asso);

        this.createTableNode(asso, TableType.Association);
        TableController assoTc = this.tableNodes.get(name);

        for (ForeignKey fk : asso.getForeignKeys()) {
            TableController entityTc = super.tableNodes.get(fk.referencedTable);
            this.drawConnection(entityTc, assoTc, fk.cardinalityValue);
        }

        super.lasso.rect.toFront();
    }

    /**
     * Édite une association existante (double-clic sur son node).
     */
    private void editAssociation(TableController assocTc) {
        List<Table> entities = this.conceptualSchema.getAllEntities();
        Table oldTable = assocTc.getTable();
        String oldName = oldTable.name;

        AssociationEditorController dialog = this.loadAssociationEditorDialog(entities, oldTable, "modifier une association");
        dialog.showAndWait();
        if (!dialog.isConfirmed()) return;

        Table newTable = dialog.getResultTable();
        String newName = newTable.name;

        if (!oldName.equals(newName) && this.conceptualSchema.nameExists(newName)) {
            CanvasController.showWarningAlert("Erreur", "Une association nommée « " + newName + " » existe déjà.");
            return;
        }

        // Supprime l'ancienne association
        this.conceptualSchema.removeAssociation(oldName);
        super.group.getChildren().remove(assocTc.getRoot());
        super.tableNodes.remove(oldName);
        this.removeConnectionsInvolving(oldName);

        // Recrée la nouvelle association
        this.conceptualSchema.addAssociation(newTable);

        this.createTableNode(newTable, TableType.Association);
        TableController newAssoTc = super.tableNodes.get(newName);

        for (ForeignKey fk : newTable.getForeignKeys()) {
            TableController entityTc = super.tableNodes.get(fk.referencedTable);
            this.drawConnection(entityTc, newAssoTc, fk.cardinalityValue);
        }

        super.lasso.rect.toFront();
    }

    private AssociationEditorController loadAssociationEditorDialog(List<Table> entities, Table table, String name) {
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle(name);
            stage.setResizable(true);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/associationEditor.fxml"));
            VBox root = loader.load();
            AssociationEditorController dialogCon = loader.getController();
            dialogCon.setData(entities, stage, table);

            stage.setScene(new Scene(root));

            return dialogCon;
        } catch (IOException e) {
            throw new Error("Une erreur est survenue lors de la création du visuel : \n" + e.getMessage(), e);
        }
    }

    /**
     * Supprime les entités et associations sélectionnées
     */
    public void deleteSelected() {
        List<TableController> selected = new ArrayList<>(super.selectionModel.getSelected());

        String message = selected.size() == 1
            ? "Supprimer « " + selected.get(0).getTable().name + " » ?"
            : "Supprimer " + selected.size() + " éléments ?";

        if (!CanvasController.showConfirmationAlert("Confirmation", "Supprimer", message)) return;

        for (TableController tc : selected) {
            String name = tc.getTable().name;

            if (tc.getType() == TableType.Entity) {
                this.conceptualSchema.removeEntity(name);
            } else {
                this.conceptualSchema.removeAssociation(name);
            }

            this.removeConnectionsInvolving(name);
            super.group.getChildren().remove(tc.getRoot());
            super.tableNodes.remove(name);
        }

        super.selectionModel.clear();
    }

    private void removeConnectionsInvolving(String tableName) {
        Iterator<Connection> it = super.connectionLines.iterator();
        while (it.hasNext()) {
            Connection c = it.next();
            if (tableName.equals(c.firstTable) || tableName.equals(c.secondTable)) {
                super.group.getChildren().removeAll(c.line, c.label);
                it.remove();
            }
        }
    }
}