package com.dbeditor.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;
import com.dbeditor.sql.file.exporter.MYSQL_Exporter;
import com.dbeditor.sql.file.parser.MYSQL_FileParser;
import com.dbeditor.util.DbManager;
import com.dbeditor.util.FileManager;
import com.dbeditor.util.ThemeManager;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;

public class CanvasController {
    private final DbManager D_M = DbManager.getInstance();
    private final ThemeManager T_M = ThemeManager.getInstance();
    private final FileManager F_M = FileManager.getInstance();

    @FXML ToolBar toolBar;
    @FXML private Button btnRedo, btnUndo;
    @FXML private TextField tfDbName;
    @FXML private Region spacer1, spacer2;
    @FXML private Pane pane;
    @FXML private Menu menuOpenDbMYSQL, menuSaveDbMYSQL;
    @FXML private MenuItem miLT, miDT, miPT;
    private Group contentGroup; 
    
    private DatabaseSchema schema;
    private List<TableController> tableNodes;

    // sélection et drag multi
    private final List<TableController> selectedTables = new ArrayList<>();
    private final Map<TableController, Point2D> dragStartPositions = new HashMap<>();
    private Point2D dragStartMouse = null;

    // zoom / pan
    private Scale scale;
    private double zoomLevel = 1.0;
    private double lastMouseX, lastMouseY;
    private boolean middleMouseDown = false;

    // lasso
    private Rectangle selectionRect;
    private Point2D selectionStart;

    @FXML
    private void initialize() throws IOException {
        this.schema = new DatabaseSchema();
        this.tableNodes = new ArrayList<>();

        this.miLT.setOnAction(e -> this.changeTheme(1));
        this.miDT.setOnAction(e -> this.changeTheme(2));
        this.miPT.setOnAction(e -> this.changeTheme(3));
        this.tfDbName.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused && this.schema != null) {
                this.schema.setName(this.tfDbName.getText());
            }
        });

        HBox.setHgrow(this.spacer1, Priority.ALWAYS);
        HBox.setHgrow(this.spacer2, Priority.ALWAYS);

        // clip pour éviter que le contenu déborde la zone centrale (ex: toolbar)
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(pane.widthProperty());
        clip.heightProperty().bind(pane.heightProperty());
        this.pane.setClip(clip);

        // recalculer clamp si la taille change
        this.pane.widthProperty().addListener((obs, oldV, newV) -> this.clampContentPosition());
        this.pane.heightProperty().addListener((obs, oldV, newV) -> this.clampContentPosition());

        // wrapper non-layoutable pour tout le contenu zoomable/pannable
        this.contentGroup = new Group();
        this.pane.getChildren().add(this.contentGroup);

        // créer le lasso (existant mais invisible)
        this.createSelectionRect();

        // setup (pan, zoom) avant création des nodes pour capter events
        this.setupPan();
        this.setupZoom();

        // créer les tables + connexions
        this.createTableNodes();
        this.drawConnections();

        // s'assurer que le lasso est au-dessus
        if (this.selectionRect != null) this.selectionRect.toFront();

        this.updateStyle();

        // clamp initial pour centrer si nécessaire
        this.clampContentPosition();
    }
    
    /**
     * Pan fiable (button middle) + lasso (left drag on background)
     * - Pan : event filters (capture phase) pour ne pas être bloqué par enfants
     * - Lasso : handlers normaux sur pane
     */
    private void setupPan() {
        // ----------- event filters pour pan avec bouton du milieu -----------
        pane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.MIDDLE) {
                middleMouseDown = true;
                lastMouseX = e.getSceneX();
                lastMouseY = e.getSceneY();
                pane.setCursor(Cursor.CLOSED_HAND);
                e.consume();
            }
        });

        pane.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (this.middleMouseDown && e.getButton() == MouseButton.MIDDLE) {
                double dx = e.getSceneX() - this.lastMouseX;
                double dy = e.getSceneY() - this.lastMouseY;

                // applique le déplacement libre pendant le drag (pas de clamp ici)
                contentGroup.setTranslateX(contentGroup.getTranslateX() + dx);
                contentGroup.setTranslateY(contentGroup.getTranslateY() + dy);

                this.lastMouseX = e.getSceneX();
                this.lastMouseY = e.getSceneY();
                e.consume();
            }
        });

        pane.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (this.middleMouseDown && e.getButton() == MouseButton.MIDDLE) {
                this.middleMouseDown = false;
                pane.setCursor(Cursor.DEFAULT);
                e.consume();
                this.clampContentPosition();
            }
        });

        // ----------- handlers normaux pour le lasso (clic gauche sur fond) -----------
        pane.setOnMousePressed(e -> {
            // si clic gauche et on n'a pas cliqué sur une table -> démarrer le lasso
            if (e.getButton() == MouseButton.PRIMARY && clickedTableController(e) == null) {
                clearSelection();
                this.selectionStart = this.contentGroup.sceneToLocal(e.getSceneX(), e.getSceneY());

                this.selectionRect.setX(this.selectionStart.getX());
                this.selectionRect.setY(this.selectionStart.getY());
                this.selectionRect.setWidth(0);
                this.selectionRect.setHeight(0);

                this.selectionRect.setVisible(true);
                this.selectionRect.toFront();

                e.consume();
            }
        });

        pane.setOnMouseDragged(e -> {
            // attention : pan middle est géré par les filters ci-dessus
            if (this.selectionRect.isVisible()) {
                Point2D cur = contentGroup.sceneToLocal(e.getSceneX(), e.getSceneY());
                double x = Math.min(this.selectionStart.getX(), cur.getX());
                double y = Math.min(this.selectionStart.getY(), cur.getY());
                double w = Math.abs(cur.getX() - this.selectionStart.getX());
                double h = Math.abs(cur.getY() - this.selectionStart.getY());

                this.selectionRect.setX(x);
                this.selectionRect.setY(y);
                this.selectionRect.setWidth(w);
                this.selectionRect.setHeight(h);

                this.selectIntersectingTables();
                e.consume();
            }
        });

        pane.setOnMouseReleased(e -> {
            if (this.selectionRect.isVisible()) {
                this.selectionRect.setVisible(false);
                e.consume();
            }
        });

        pane.setOnMouseExited(e -> {
            if (this.middleMouseDown) {
                this.middleMouseDown = false;
                this.pane.setCursor(Cursor.DEFAULT);
            }
        });
    }

    private void selectIntersectingTables() {
        // selectionRect et tables sont enfants du même parent (contentGroup)
        Bounds rectBounds = selectionRect.getBoundsInParent();
        clearSelection();

        for (TableController tc : tableNodes) {
            if (tc.getRoot().getBoundsInParent().intersects(rectBounds)) {
                select(tc);
            }
        }
    }

    private TableController clickedTableController(MouseEvent e) {
        Node target = (Node) e.getTarget();
        while (target != null && target != this.pane && target != this.contentGroup) {
            for (TableController tc : tableNodes) {
                if (tc.getRoot() == target) {
                    return tc;
                }
            }
            target = target.getParent();
        }
        return null;
    }

    /**
     * Zoom centré sur la souris (compensation correcte)
     */
    private void setupZoom() {
        this.scale = new Scale(1, 1, 0, 0);
        this.contentGroup.getTransforms().add(this.scale);

        this.pane.setOnScroll(e -> {
            e.consume();

            double deltaFactor = e.getDeltaY() > 0 ? 1.1 : 0.9;
            // position du point sous la souris avant zoom (coord locales)
            Point2D mouseScene = new Point2D(e.getSceneX(), e.getSceneY());
            Point2D mouseLocal = contentGroup.sceneToLocal(mouseScene);

            // appliquer la nouvelle échelle
            this.zoomLevel = Math.max(0.1, Math.min(this.zoomLevel * deltaFactor, 3.0));
            this.scale.setX(this.zoomLevel);
            this.scale.setY(this.zoomLevel);

            // calculer où le point local est rendu en scene après zoom
            Point2D newMouseScene = contentGroup.localToScene(mouseLocal);

            // delta = position après - position souris -> compenser en translant
            double dx = newMouseScene.getX() - mouseScene.getX();
            double dy = newMouseScene.getY() - mouseScene.getY();

            this.contentGroup.setTranslateX(this.contentGroup.getTranslateX() - dx);
            this.contentGroup.setTranslateY(this.contentGroup.getTranslateY() - dy);

            // clamp après ajustement (NE RECENTRE PAS si contenu plus petit)
            Platform.runLater(() -> clampContentPosition());
        });
    }

    /**
     * Création des nodes (tables). Branche callbacks de drag pour multi-move.
     */
    private void createTableNodes() throws IOException {
        int col = 0, row = 0;
        int cols = (int) Math.ceil(Math.sqrt(this.schema.getTables().size()));
        
        for (Table table : this.schema.getTables().values()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/table.fxml"));
            AnchorPane nodePane = loader.load();
            TableController nodeController = loader.getController();
            nodeController.createTableNode(table);

            // brancher callbacks : sélection + drag multi + drag end
            nodeController.setOnSelect((tc, ev) -> handleSelection(tc, ev));
            nodeController.setOnDrag((tc, ev) -> handleDrag(tc, ev));
            nodeController.setOnDragEnd((tc, ev) -> handleDragEnd(tc, ev));

            nodePane.setLayoutX(col * 350 + 50);
            nodePane.setLayoutY(row * 250 + 50);

            this.contentGroup.getChildren().add(nodePane);
            this.tableNodes.add(nodeController);
            
            col++;
            if (col >= cols) {
                col = 0;
                row++;
            }
        }

        // s'assurer que le lasso reste au-dessus
        if (this.selectionRect != null) this.selectionRect.toFront();
    }

    /**
     * Logique de sélection : Ctrl+clic toggle, sinon :
     * - si la table cliquée fait déjà partie de la sélection -> garder la sélection (pour permettre drag multi)
     * - sinon -> selection unique
     */
    private void handleSelection(TableController tc, MouseEvent e) {
        if (e.isControlDown()) {
            toggleSelection(tc);
            return;
        }

        if (selectedTables.contains(tc)) {
            // la table est déjà sélectionnée -> ne pas effacer la sélection
            tc.getRoot().toFront();
            return;
        }

        clearSelection();
        select(tc);
    }

    private void select(TableController tc) {
        if (!selectedTables.contains(tc)) {
            selectedTables.add(tc);
            tc.setSelected(true);
        }
    }

    private void toggleSelection(TableController tc) {
        if (selectedTables.contains(tc)) {
            selectedTables.remove(tc);
            tc.setSelected(false);
        } else {
            select(tc);
        }
    }

    private void clearSelection() {
        selectedTables.forEach(t -> t.setSelected(false));
        selectedTables.clear();
    }

    private void drawConnections() {
        for (TableController fromNode : this.tableNodes) {
            Table fromTable = fromNode.getTable();
            for (ForeignKey fk : fromTable.getForeignKeys()) {
                TableController toNode = this.findTableNode(fk.getReferencedTable());
                if (toNode != null) {
                    drawConnection(fromNode, toNode);
                }
            }
        }
    }

    private TableController findTableNode(String tableName) {
        for (TableController node : this.tableNodes) {
            if (node.getTable().getName().equals(tableName)) {
                return node;
            }
        }
        return null;
    }
    
    private void drawConnection(TableController from, TableController to) {
        double fromX = from.getRoot().getLayoutX() + from.getRoot().getWidth() / 2;
        double fromY = from.getRoot().getLayoutY() + from.getRoot().getHeight() / 2;
        double toX = to.getRoot().getLayoutX() + to.getRoot().getWidth() / 2;
        double toY = to.getRoot().getLayoutY() + to.getRoot().getHeight() / 2;
        
        Line line = new Line(fromX, fromY, toX, toY);
        line.setStroke(Color.web(T_M.getTheme().getSecondaryTextColor()));
        line.setStrokeWidth(2);
        line.getStrokeDashArray().addAll(5.0, 5.0);
        
        this.contentGroup.getChildren().add(0, line);
        bindLine(line, from, to);
    }

    private void bindLine(Line line, TableController from, TableController to) {
        line.startXProperty().bind(
            from.getRoot().layoutXProperty()
                .add(from.getRoot().widthProperty().divide(2))
        );

        line.startYProperty().bind(
            from.getRoot().layoutYProperty()
                .add(from.getRoot().heightProperty().divide(2))
        );

        line.endXProperty().bind(
            to.getRoot().layoutXProperty()
                .add(to.getRoot().widthProperty().divide(2))
        );

        line.endYProperty().bind(
            to.getRoot().layoutYProperty()
                .add(to.getRoot().heightProperty().divide(2))
        );
    }

    private void createSelectionRect() {
        this.selectionRect = new Rectangle();
        this.selectionRect.setManaged(false);
        this.selectionRect.setMouseTransparent(true);
        this.selectionRect.setFill(Color.web("#4A90E235"));
        this.selectionRect.setStroke(Color.web("#4A90E2"));
        this.selectionRect.setStrokeWidth(1.5);
        this.selectionRect.setVisible(false);
        this.contentGroup.getChildren().add(this.selectionRect);
    }

    // -------------------------
    // Drag handling (multi-move)
    // -------------------------
    private void handleDrag(TableController tc, MouseEvent e) {
        // position de la souris dans les coordonnées du contentGroup
        Point2D mouseInParent = contentGroup.sceneToLocal(e.getSceneX(), e.getSceneY());

        // début du drag : initialiser si besoin
        if (dragStartMouse == null) {
            dragStartMouse = mouseInParent;
            dragStartPositions.clear();

            // si table cliquée n'est pas dans la sélection -> sélectionner uniquement elle
            if (!selectedTables.contains(tc)) {
                clearSelection();
                select(tc);
            }

            // stocker les positions de départ de chaque table sélectionnée
            for (TableController selected : selectedTables) {
                dragStartPositions.put(selected, new Point2D(
                    selected.getRoot().getLayoutX(), selected.getRoot().getLayoutY()));
                selected.getRoot().toFront(); // pour l'UX
            }
        }

        // calcul du delta
        double dx = mouseInParent.getX() - dragStartMouse.getX();
        double dy = mouseInParent.getY() - dragStartMouse.getY();

        // appliquer le déplacement relatif à toutes les tables sélectionnées
        for (Map.Entry<TableController, Point2D> entry : dragStartPositions.entrySet()) {
            TableController t = entry.getKey();
            Point2D start = entry.getValue();
            t.getRoot().setLayoutX(start.getX() + dx);
            t.getRoot().setLayoutY(start.getY() + dy);
        }

        e.consume();
    }

    private void handleDragEnd(TableController tc, MouseEvent e) {
        // cleanup
        dragStartPositions.clear();
        dragStartMouse = null;
        e.consume();
    }

    private void changeTheme(int idTheme) {
        T_M.changeTheme(idTheme);
        this.updateStyle();
    }

    private void updateStyle() {
        this.pane.setStyle("-fx-background-color: " + T_M.getTheme().getBackgroundColor() + ";");

        this.toolBar.setStyle("-fx-background-color: " + T_M.getTheme().getToolbarColor() + 
                        "; -fx-border-color: " + T_M.getTheme().getToolbarBorderColor() + 
                        "; -fx-border-width: 0 0 1 0;");

        for(TableController table : this.tableNodes) {
            table.updateStyle();
        }
    }

    /**
     * Contrainte pour que le contenu reste dans la vue.
     * IMPORTANT: ne recentre PAS automatiquement si le contenu est plus petit que le viewport,
     * afin de préserver le focal point du zoom.
     */
    private void clampContentPosition() {
        if (contentGroup == null || pane == null) return;

        // bounds réels du contenu rendu (inclut scale & translate)
        Bounds b = contentGroup.getBoundsInParent();
        double paneW = pane.getWidth();
        double paneH = pane.getHeight();

        double tx = contentGroup.getTranslateX();
        double ty = contentGroup.getTranslateY();

        // marge minimale visible du contenu (en pixels)
        final double minVisible = 40.0;

        // si tout le contenu est complètement à gauche de la vue -> le ramener pour qu'au moins minVisible soit visible
        if (b.getMaxX() < minVisible) {
            // décaler de la différence
            tx += (minVisible - b.getMaxX());
        }
        // si tout le contenu est complètement à droite de la vue
        if (b.getMinX() > paneW - minVisible) {
            tx -= (b.getMinX() - (paneW - minVisible));
        }

        // vertical : si tout en haut
        if (b.getMaxY() < minVisible) {
            ty += (minVisible - b.getMaxY());
        }
        // vertical : si tout en bas
        if (b.getMinY() > paneH - minVisible) {
            ty -= (b.getMinY() - (paneH - minVisible));
        }

        // n'appliquer que si différence notable (évite repaint inutile)
        if (Math.abs(tx - contentGroup.getTranslateX()) > 0.1) {
            contentGroup.setTranslateX(tx);
        }
        if (Math.abs(ty - contentGroup.getTranslateY()) > 0.1) {
            contentGroup.setTranslateY(ty);
        }
    }

    @FXML
    void openPersoThemeParameter(ActionEvent event) {
        // TODO
    }

    void openDbMYSQL(String dbName) throws IOException {
        this.open(this.D_M.getMysqlDb().loadDb(dbName));
    }

    @FXML
    void openFileMYSQL(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ouvrir une base de données");

        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers MYSQL", "*.sql")
        );

        this.open(this.F_M.openDatabase(fileChooser, new MYSQL_FileParser()));
    }

    private void open(DatabaseSchema dbS) throws IOException {
        if(dbS != null) {
            this.tableNodes.clear();

            // Ne pas supprimer le selectionRect
            this.contentGroup.getChildren().removeIf(node -> node != this.selectionRect);

            this.schema = dbS;
            this.tfDbName.setText(dbS.getName());

            createTableNodes();
            drawConnections();

            if (this.selectionRect != null) this.selectionRect.toFront();
            clampContentPosition();
        }
    }

    @FXML
    void saveDbMYSQL(ActionEvent event) {
        System.out.println("saveDbMYSQL");
    }

    @FXML
    void saveFileMYSQL(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en SQL");

        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers SQL", "*.sql")
        );
        fileChooser.setInitialFileName("export.sql");

        this.F_M.exportSQL(fileChooser, this.schema, new MYSQL_Exporter());
    }
}