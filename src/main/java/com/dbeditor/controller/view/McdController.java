package com.dbeditor.controller.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dbeditor.MainApp;
import com.dbeditor.controller.CanvasController;
import com.dbeditor.controller.TableController;
import com.dbeditor.controller.TableController.TableType;
import com.dbeditor.controller.ViewType;
import com.dbeditor.controller.view.dialogs.AssociationEditorDialog;
import com.dbeditor.controller.view.dialogs.TableEditorDialog;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.Table;
import com.dbeditor.model.mcd.CardinalityValue;
import com.dbeditor.model.mcd.ConceptualSchema;
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
    private static final ThemeManager T_M = ThemeManager.getInstance();
    
    @Override
    public ViewType getViewType() {
        return ViewType.MCD;
    }

    private Button btnEntity, btnAssociation;

    // Modèle de données MCD
    private ConceptualSchema conceptualSchema;
    
    @Override
    public void initialization(ToolBar toolbar) {
        this.conceptualSchema = new ConceptualSchema(MainApp.getSchema());

        this.btnEntity = super.createButton("Entité");
        this.btnAssociation = super.createButton("Association");
        toolbar.getItems().addAll(this.btnEntity, this.btnAssociation);

        super.initialization(toolbar);

        ChangeListener<Scene> sceneListener = (obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.DELETE) {
                        try {
                            this.deleteSelected();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                });
            }
        };
        this.getRoot().sceneProperty().addListener(sceneListener);

        // basic button handlers (could be expanded)
        this.btnEntity.setOnAction(e -> {
            try { addEntity(); } catch (IOException ex) { ex.printStackTrace(); }
        });
        this.btnAssociation.setOnAction(e -> {
            try { addAssociation(); } catch (IOException ex) { ex.printStackTrace(); }
        });
    }

    @Override
    public void open(DatabaseSchema dbS) throws IOException {
        if (dbS == null) return;

        this.conceptualSchema = new ConceptualSchema(dbS);

        // supprime tous les nodes sauf selectionRect
        super.getGroup().getChildren().removeIf(node -> node != super.getLasso().getRect());

        // vider les structures
        super.getTableNodes().clear();
        super.getConnectionLines().clear();

        // créer les nodes à partir du MCD
        this.createTableNodes();
        this.drawLinks();

        if (super.getLasso() != null) {
            super.getLasso().getRect().toFront();
        }

        super.updateStyle();
    }

    /**
     * Crée les nodes visuels pour les entités
     */
    private void createTableNodes() throws IOException {
        // TODO: script de rangement automatique
        for (Table table : this.conceptualSchema.getTables()) {
            this.createTableNode(table, TableType.Entite);
        }

        // s'assure que le rectangle de séléction est devant
        if (super.getLasso() != null) {
            super.getLasso().getRect().toFront();
        }
    }

    /**
     * Crée un node d'entité à une position donnée
     */
    private void createTableNode(Table table, TableType tabletype) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/table.fxml"));
        AnchorPane tcPane = loader.load();
        TableController tcController = loader.getController();
        tcController.createTableNode(table, tabletype);

        super.getTableNodes().put(tcController.getTable().getName(), tcController);

        // Gérer la sélection
        tcController.setOnSelect((tc, e) -> super.handleSelection(tc, e));

        // Menu contextuel
        tcPane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                // si double clique gauche -> modifier la table ou l'association
                if (e.getClickCount() == 2) {
                    if(tabletype.equals(TableType.Entite)) {
                        // this.editTable(tcController);
                    } else {
                        // this.editAssociation(tcController);
                    }
                    e.consume();
                }
            }
        });

        // attache le node pour le multidrag
        super.getMultiDrag().attach(tcController);

        super.getGroup().getChildren().add(tcPane);
    }

    /**
     * Tracer tout les liens entre les tables
     * @throws IOException 
     */
    private void drawLinks() throws IOException {
        // Supprimer les anciennes lignes
        super.getConnectionLines().forEach(pair -> {
            super.getGroup().getChildren().remove(pair.getKey());
            super.getGroup().getChildren().remove(pair.getValue());
        });
        super.getConnectionLines().clear();

        Map<String, List<Pair<Table, CardinalityValue>>> links = this.conceptualSchema.getLinks();
        for (String name : links.keySet()) {
            Table table = this.conceptualSchema.getAssociationTable(name);

            // centre l'association
            for(Pair<Table, CardinalityValue> p : links.get(name)) {
                table.setPosX(table.getPosX() + p.getKey().getPosX());
                table.setPosY(table.getPosY() + p.getKey().getPosY());
            }
            table.setPosX(table.getPosX() / links.get(name).size());
            table.setPosY(table.getPosY() / links.get(name).size());

            this.createTableNode(table, TableType.Association);
            TableController ac = super.getTableNodes().get(name);

            for(Pair<Table, CardinalityValue> p : links.get(name)) {
                this.drawLink(super.getTableNodes().get(p.getKey().getName()), ac, p.getValue());
            }
        }
    }

    /**
     * Permet de tracer un lien entre une entité et une association
     */
    private void drawLink(TableController from, TableController to, CardinalityValue cardinality) {
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
    
        // Texte de la cardinalité
        Label cardinalityLabel = new Label(cardinality.toString());
        cardinalityLabel.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (e.getClickCount() == 2) {
                    this.editCardinality(cardinalityLabel, from, to);
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
        super.getGroup().getChildren().add(0, cardinalityLabel);
        super.getGroup().getChildren().add(0, line);
        super.getConnectionLines().add(new Pair<>(line, cardinalityLabel));
    }

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
    public void addEntity() throws IOException {
        TableEditorDialog dialog = new TableEditorDialog(null);
        dialog.showAndWait();
        if (!dialog.isConfirmed()) return;
        
        Table table = dialog.getResultTable();
        
        if (this.conceptualSchema.nameExists(table.getName())) {
            CanvasController.showWarningAlert("Erreur", "Ce nom est déja utilisé.");
            return;
        }

        this.conceptualSchema.addEntity(table);
        
        this.createTableNode(table, TableType.Entite);
    }

    // /**
    //  * Édite une entité existante.
    //  */
    // private void editEntity(TableController tc) {
    //     Table oldTable = tc.getTable();
    //     String oldName = oldTable.getName();

    //     TableEditorDialog dialog = new TableEditorDialog(oldTable);
    //     dialog.showAndWait();
    //     if (!dialog.isConfirmed()) return;

    //     Table modifiedTable = dialog.getResultTable();
    //     String newName = modifiedTable.getName();

    //     if (!oldName.equals(newName) && this.conceptualSchema.getEntityTable(newName) != null) {
    //         CanvasController.showWarningAlert("Erreur", "Une entité nommée « " + newName + " » existe déjà.");
    //         return;
    //     }

    //     // Mettre à jour dans le ConceptualSchema (gère aussi les associations)
    //     this.conceptualSchema.renameEntity(oldName, modifiedTable);

    //     // Mettre à jour dans le DatabaseSchema global
    //     MainApp.getSchema().removeTable(oldName);
    //     MainApp.getSchema().addTable(modifiedTable);

    //     try { this.rebuildView(); } catch (IOException e) { e.printStackTrace(); }
    // }

    /**
     * Ajoute une nouvelle association.
     */
    public void addAssociation() throws IOException {
        List<Table> entities = this.conceptualSchema.getTables();
        if (entities.size() < 1) {
            CanvasController.showWarningAlert("Erreur", "Il faut au moins 1 entité pour créer une association.");
            return;
        }

        AssociationEditorDialog dialog = new AssociationEditorDialog(entities);
        dialog.showAndWait();
        if (!dialog.isConfirmed()) return;

        Pair<String, List<Pair<String, CardinalityValue>>> result = dialog.getResultAssociation();

        if (this.conceptualSchema.nameExists(result.getKey())) {
            CanvasController.showWarningAlert("Erreur", "Ce nom est déja utilisé.");
            return;
        }

        Table asso = this.conceptualSchema.addAssociation(result.getKey(), result.getValue());
        this.createTableNode(asso, TableType.Association);
        TableController assoCon = this.getTableNodes().get(asso.getName());

        for(Pair<String, CardinalityValue> p : result.getValue()) {
            this.drawLink(this.getTableNodes().get(p.getKey()), assoCon, p.getValue());
        }

        super.getLasso().getRect().toFront();
    }

    // /**
    //  * Édite une association existante (double-clic sur son node).
    //  */
    // private void editAssociation(TableController assocTc) {
    //     String oldName = assocTc.getTable().getName();

    //     // Trouver l'association dans le modèle
    //     ConceptualSchema.Association target = null;
    //     for (ConceptualSchema.Association a : this.conceptualSchema.getAssociations()) {
    //         if (a.name.equals(oldName)) { target = a; break; }
    //     }
    //     if (target == null) return;

    //     List<ConceptualSchema.Entity> entities = this.conceptualSchema.getEntities();
    //     AssociationEditorDialog dialog = new AssociationEditorDialog(
    //             entities, oldName, target.linkedEntities);
    //     dialog.showAndWait();
    //     if (!dialog.isConfirmed()) return;

    //     String newName = dialog.getResultName();
    //     Map<ConceptualSchema.Entity, CardinalityValue> newParticipants = dialog.getResultParticipants();

    //     this.conceptualSchema.updateAssociation(oldName, newName, newParticipants);

    //     try { this.drawAssociations(); } catch (IOException e) { e.printStackTrace(); }
    //     if (super.getLasso() != null) super.getLasso().getRect().toFront();
    // }

    /**
     * Supprime les entités et associations sélectionnées
     */
    public void deleteSelected() throws IOException {
        List<TableController> selected = new ArrayList<>(super.getSelectionModel().getSelected());
        if (selected.isEmpty()) return;

        String message = selected.size() == 1
            ? "Supprimer « " + selected.get(0).getTable().getName() + " » ?"
            : "Supprimer " + selected.size() + " éléments ?";

        if (!CanvasController.showConfirmationAlert("Confirmation", "Supprimer", message)) return;

        for (TableController tc : selected) {
            String name = tc.getTable().getName();

            if (this.conceptualSchema.getTable(name) != null) {
                // Entité : supprimer du MCD et du schema global
                this.conceptualSchema.removeEntity(name);
                MainApp.getSchema().removeTable(name);
            } else {
                // Association
                this.conceptualSchema.removeAssociation(name);
            }

            super.getGroup().getChildren().remove(tc.getRoot());
            super.getTableNodes().remove(name);
        }

        super.getSelectionModel().clear();
        // this.drawAssociations();
    }
}