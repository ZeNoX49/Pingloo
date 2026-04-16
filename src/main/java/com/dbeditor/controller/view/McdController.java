package com.dbeditor.controller.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.dbeditor.MainApp;
import com.dbeditor.controller.CanvasController;
import com.dbeditor.controller.TableController;
import com.dbeditor.controller.TableController.TableType;
import com.dbeditor.controller.ViewType;
import com.dbeditor.controller.view.dialogs.AssociationEditorDialog;
import com.dbeditor.controller.view.dialogs.EntityEditorDialog;
import com.dbeditor.model.Table;
import com.dbeditor.model.mcd.CardinalityValue;
import com.dbeditor.model.mcd.ConceptualSchema;
import com.dbeditor.sql.DbType;
import com.dbeditor.util.ThemeManager;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Pair;

public class McdController extends ModelView {
    private static final Logger LOGGER = Logger.getLogger(McdController.class.getName());

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

        ChangeListener<Scene> sceneListener = (obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.DELETE) {
                        this.deleteSelected();
                    }
                });
            }
        };
        this.getRoot().sceneProperty().addListener(sceneListener);

        // basic button handlers (could be expanded)
        this.btnEntity.setOnAction(e -> {
            this.addEntity();
        });
        this.btnAssociation.setOnAction(e -> {
            this.addAssociation();
        });
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
        this.drawLinks();

        super.lasso.rect.toFront();

        super.updateStyle();
    }

    @Override
    public void updateType(DbType type) {
        // TODO
    }

    /**
     * Crée les nodes visuels pour les entités
     */
    private void createTableNodes() {
        // TODO: script de rangement automatique
        for (Table table : this.conceptualSchema.getEntitiesTables()) {
            this.createTableNode(table, TableType.Entity);
        }
    }

    /**
     * Crée un node d'entité à une position donnée
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

        super.tableNodes.put(tcController.getTable().name, tcController);

        // Gérer la sélection
        tcController.setOnSelect((tc, e) -> super.handleSelection(tc, e));

        // Menu contextuel
        tcPane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                // si double clique gauche -> modifier la table ou l'association
                if (e.getClickCount() == 2) {
                    if(tabletype.equals(TableType.Entity)) {
                        this.editEntity(tcController);
                    } else {
                        this.editAssociation(tcController); 
                    }
                    e.consume();
                }
            }
        });

        // mettre a jour la position lors d'un déplacement
        tcPane.layoutXProperty().addListener((obs, oldX, newX) -> {
            table.posX = newX.doubleValue();
        });
        tcPane.layoutYProperty().addListener((obs, oldY, newY) -> {
            table.posY = newY.doubleValue();
        });

        // attache le node pour le multidrag
        super.multiDrag.attach(tcController);

        super.group.getChildren().add(tcPane);
    }

    /**
     * Tracer tout les liens entre les tables
     */
    private void drawLinks() {
        // Supprimer les anciennes lignes
        super.connectionLines.forEach(connection -> {
            super.group.getChildren().remove(connection.line);
            super.group.getChildren().remove(connection.label);
        });
        super.connectionLines.clear();

        Map<String, List<Pair<Table, CardinalityValue>>> links = this.conceptualSchema.getLinks();
        for (String name : links.keySet()) {
            Table table = this.conceptualSchema.getAssociationTable(name);

            // centre l'association
            double sumX = 0;
            double sumY = 0;

            for (Pair<Table, CardinalityValue> p : links.get(name)) {
                sumX += p.getKey().posX;
                sumY += p.getKey().posY;
            }

            table.posX = sumX / links.get(name).size();
            table.posY = sumY / links.get(name).size();

            this.createTableNode(table, TableType.Association);
            TableController ac = super.tableNodes.get(name);

            for(Pair<Table, CardinalityValue> p : links.get(name)) {
                this.drawLink(super.tableNodes.get(p.getKey().name), ac, p.getValue());
            }
        }
    }

    /**
     * Permet de tracer un lien entre une entité et une association
     */
    private void drawLink(TableController fromEntity, TableController toAsso, CardinalityValue cardinality) {
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
        cardinalityLabel.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (e.getClickCount() == 2) {
                    this.editCardinality(cardinalityLabel, fromEntity, toAsso);
                }
            }
        });

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

    // TODO
    /**
     *   // Édition inline de la cardinalité (double-clic sur le label).
     *   // Change la cardinalité dans le ConceptualSchema et rafraîchit.
     */
    private void editCardinality(Label cardLabel, TableController entityNode, TableController assocNode) {
        // CardinalityValue[] values = CardinalityValue.values();
        // String current = cardLabel.getText();
        // CardinalityValue currentCard = CardinalityValue.getCardinalityValue(current);
        // int idx = 0;
        // for (int i = 0; i < values.length; i++) {
        //     if (values[i] == currentCard) { idx = i; break; }
        // }
        // CardinalityValue next = values[(idx + 1) % values.length];

        // // Mettre à jour dans le modèle
        // String entityName = entityNode.getTable().getName();
        // String assocName  = assocNode.getTable().getName();

        // // for (ConceptualSchema.Association assoc : this.conceptualSchema.getAssociations()) {
        // //     if (assoc.name.equals(assocName)) {
        // //         for (ConceptualSchema.Entity e : assoc.linkedEntities.keySet()) {
        // //             if (e.table.getName().equals(entityName)) {
        // //                 assoc.linkedEntities.put(e, next);
        // //                 break;
        // //             }
        // //         }
        // //     }
        // // }

        // cardLabel.setText(next.toString());
    }

    /**
     * Ajoute une nouvelle entité
     */
    @FXML
    public void addEntity() {
        EntityEditorDialog dialog = new EntityEditorDialog();
        dialog.showAndWait();
        if (!dialog.isConfirmed()) return;
        
        Table table = dialog.getResultTable();
        
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
    
        EntityEditorDialog dialog = new EntityEditorDialog(oldTable);
        dialog.showAndWait();
        if (!dialog.isConfirmed()) return;

        Table modifiedTable = dialog.getResultTable();
        String newName = modifiedTable.name;

        if (!oldName.equals(newName) && this.conceptualSchema.getEntityTable(newName) != null) {
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
                it.remove(); // suppression safe pendant l'itération
            }
        }

        // Recrée le node avec le nouveau nom
        this.createTableNode(modifiedTable, TableType.Entity);

        TableController newTc = super.tableNodes.get(newName);

        // Redessine uniquement les liens de cette entité
        for (Entry<String, List<Pair<Table, CardinalityValue>>> entry : this.conceptualSchema.getLinks().entrySet()) {
            String assocName = entry.getKey();
            TableController assocTc = super.tableNodes.get(assocName);
            if (assocTc == null) continue;

            for (Pair<Table, CardinalityValue> p : entry.getValue()) {
                if (p.getKey().name.equals(newName)) {
                    this.drawLink(newTc, assocTc, p.getValue());
                    break;
                }
            }
        }
    }

    /**
     * Ajoute une nouvelle association.
     */
    public void addAssociation() {
        List<Table> entities = this.conceptualSchema.getEntitiesTables();
        if (entities.isEmpty()) {
            CanvasController.showWarningAlert("Erreur", "Il faut au moins 1 entité pour créer une association.");
            return;
        }

        AssociationEditorDialog dialog = new AssociationEditorDialog(entities, null);
        dialog.showAndWait();
        if (!dialog.isConfirmed()) return;

        Pair<String, List<Pair<String, CardinalityValue>>> result = dialog.getResultAssociation();
        String name = result.getKey();

        if (this.conceptualSchema.nameExists(name)) {
            CanvasController.showWarningAlert("Erreur", "Ce nom est déja utilisé.");
            return;
        }

        Table asso = this.conceptualSchema.addAssociation(name, result.getValue());
        this.createTableNode(asso, TableType.Association);
        TableController assoCon = this.tableNodes.get(asso.name);

        for(Pair<String, CardinalityValue> p : result.getValue()) {
            this.drawLink(this.tableNodes.get(p.getKey()), assoCon, p.getValue());
        }

        super.lasso.rect.toFront();
    }

    // TODO
    /**
     * Édite une association existante (double-clic sur son node).
     */
    private void editAssociation(TableController assocTc) {
        List<Table> entities = this.conceptualSchema.getEntitiesTables();
        Table oldTable = assocTc.getTable();
        String oldName = oldTable.name;

        Pair<String, List<Pair<Table, CardinalityValue>>> current = new Pair<>(oldName, this.conceptualSchema.getLinks().get(oldName));
        AssociationEditorDialog dialog = new AssociationEditorDialog(entities, current);
        dialog.showAndWait();
        if (!dialog.isConfirmed()) return;

        Pair<String, List<Pair<String, CardinalityValue>>> result = dialog.getResultAssociation();
        String newName = result.getKey();

        if (!oldName.equals(newName) && this.conceptualSchema.nameExists(newName)) {
            CanvasController.showWarningAlert("Erreur", "Une association nommée « " + newName + " » existe déjà.");
            return;
        }

        // On garde la position visuelle actuelle
        double savedX = assocTc.getRoot().getLayoutX();
        double savedY = assocTc.getRoot().getLayoutY();

        // Supprime l'ancienne association
        this.conceptualSchema.removeAssociation(oldName);
        MainApp.schema.tables.remove(oldName);

        super.group.getChildren().remove(assocTc.getRoot());
        super.tableNodes.remove(oldName);

        // Supprime tous les liens liés à cette association
        Iterator<Connection> it = super.connectionLines.iterator();
        while (it.hasNext()) {
            Connection connection = it.next();

            if (connection.firstTable.equals(oldName) || connection.secondTable.equals(oldName)) {
                super.group.getChildren().removeAll(connection.line, connection.label);
                it.remove();
            }
        }

        // Recrée la nouvelle association
        Table newAsso = this.conceptualSchema.addAssociation(newName, result.getValue());
        newAsso.posX = savedX;
        newAsso.posY = savedY;

        this.createTableNode(newAsso, TableType.Association);

        TableController newAssoTc = super.tableNodes.get(newName);
        newAssoTc.getRoot().setLayoutX(savedX);
        newAssoTc.getRoot().setLayoutY(savedY);

        for (Pair<String, CardinalityValue> p : result.getValue()) {
            TableController entityTc = super.tableNodes.get(p.getKey());
            this.drawLink(entityTc, newAssoTc, p.getValue());
        }

        super.lasso.rect.toFront();
    }

    /**
     * Supprime les entités et associations sélectionnées
     */
    public void deleteSelected() {
        List<TableController> selected = new ArrayList<>(super.selectionModel.getSelected());
        if (selected.isEmpty()) return;

        String message = selected.size() == 1
            ? "Supprimer « " + selected.get(0).getTable().name + " » ?"
            : "Supprimer " + selected.size() + " éléments ?";

        if (!CanvasController.showConfirmationAlert("Confirmation", "Supprimer", message)) return;

        for (TableController tc : selected) {
            String name = tc.getTable().name;

            if (this.conceptualSchema.getEntityTable(name) != null) {
                // Entité : supprimer du MCD et du schema global
                this.conceptualSchema.removeEntity(name);
            } else {
                // Association
                this.conceptualSchema.removeAssociation(name);
            }

            super.group.getChildren().remove(tc.getRoot());
            super.tableNodes.remove(name);

            Iterator<Connection> it = super.connectionLines.iterator();
            while (it.hasNext()) {
                Connection connection = it.next();
                if (connection.firstTable.equals(name) || connection.secondTable.equals(name)) {
                    super.group.getChildren().removeAll(connection.line, connection.label);
                    it.remove();
                }
            }
        }

        super.selectionModel.clear();
    }
}