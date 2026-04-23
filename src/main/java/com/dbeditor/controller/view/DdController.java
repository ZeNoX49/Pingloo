package com.dbeditor.controller.view;

import com.dbeditor.controller.ViewType;

import javafx.scene.control.ToolBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class DdController extends GridView {

    // private static final double W_TABLE = 0.10;
    // private static final double W_NAME = 0.15;
    // private static final double W_DESC = 0.65;
    // private static final double W_TYPE = 0.05;
    // private static final double W_NATURE = 0.05;

    // private static class Data {
    //     private final SimpleStringProperty table;
    //     private final SimpleStringProperty name;
    //     private final SimpleStringProperty description;
    //     private final ObjectProperty<DD_Type> type;
    //     private final ObjectProperty<DD_Nature> nature;

    //     public Data(String table, String name, String description, DD_Type type, DD_Nature nature) {
    //         this.table = new SimpleStringProperty(table);
    //         this.name = new SimpleStringProperty(name);
    //         this.description = new SimpleStringProperty(description);
    //         this.type = new SimpleObjectProperty<>(type);
    //         this.nature = new SimpleObjectProperty<>(nature);
    //     }

    //     public String getTable() { return table.get(); }
    //     public void setTable(String value) { table.set(value); }
    //     public StringProperty tableProperty() { return table; }

    //     public String getName() { return name.get(); }
    //     public void setName(String value) { name.set(value); }
    //     public StringProperty nameProperty() { return name; }

    //     public String getDescription() { return description.get(); }
    //     public void setDescription(String value) { description.set(value); }
    //     public StringProperty descriptionProperty() { return description; }

    //     public DD_Type getType() { return type.get(); }
    //     public void setType(DD_Type value) { type.set(value); }
    //     public ObjectProperty<DD_Type> typeProperty() { return type; }

    //     public DD_Nature getNature() { return nature.get(); }
    //     public void setNature(DD_Nature value) { nature.set(value); }
    //     public ObjectProperty<DD_Nature> natureProperty() { return nature; }
    // }

    private StackPane root;
    // private TableView<Data> tableView;

    @Override
    public ViewType getViewType() {
        return ViewType.DD;
    }

    @Override
    public void initialization(ToolBar toolbar) {
        // this.tableView = new TableView<>();
        // this.tableView.setEditable(true);
        // this.tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // setupTableColumns();

        // this.root = new StackPane(this.tableView);
    }

    // private void setupTableColumns() {
    //     TableColumn<Data, String> colTable = this.createTextColumn("Table", Data::tableProperty, Data::setTable, W_TABLE);
    //     TableColumn<Data, String> colName = this.createTextColumn("Nom", Data::nameProperty, Data::setName, W_NAME);
    //     TableColumn<Data, String> colDesc = this.createTextColumn("Description", Data::descriptionProperty, Data::setDescription, W_DESC);
    //     TableColumn<Data, DD_Type> colType = this.createEnumColumn("Type", Data::typeProperty, DD_Type.values(), Data::setType, W_TYPE);
    //     TableColumn<Data, DD_Nature> colNature = this.createEnumColumn("Nature", Data::natureProperty, DD_Nature.values(), Data::setNature, W_NATURE);

    //     this.tableView.getColumns().setAll(colTable, colName, colDesc, colType, colNature);
    // }

    // private TableColumn<Data, String> createTextColumn(String title, Function<Data, StringProperty> propertyGetter, BiConsumer<Data, String> setter, double widthRatio) {
    //     TableColumn<Data, String> col = new TableColumn<>(title);
    //     col.setCellValueFactory(cellData -> propertyGetter.apply(cellData.getValue()));
    //     col.setCellFactory(TextFieldTableCell.forTableColumn());
    //     col.setOnEditCommit(e -> setter.accept(e.getRowValue(), e.getNewValue()));
    //     col.setEditable(true);
    //     col.setResizable(false);
    //     col.setReorderable(false);
    //     col.setSortable(false);
    //     col.prefWidthProperty().bind(this.tableView.widthProperty().multiply(widthRatio));
    //     return col;
    // }

    // private <E extends Enum<E>> TableColumn<Data, E> createEnumColumn(String title, Function<Data, ObjectProperty<E>> propertyGetter, E[] values, BiConsumer<Data, E> setter, double widthRatio) {
    //     TableColumn<Data, E> col = new TableColumn<>(title);
    //     col.setCellValueFactory(cellData -> propertyGetter.apply(cellData.getValue()));
    //     col.setCellFactory(ComboBoxTableCell.forTableColumn(values));
    //     col.setOnEditCommit(e -> setter.accept(e.getRowValue(), e.getNewValue()));
    //     col.setEditable(true);
    //     col.setResizable(false);
    //     col.setReorderable(false);
    //     col.setSortable(false);
    //     col.setStyle("-fx-alignment: CENTER;");
    //     col.prefWidthProperty().bind(this.tableView.widthProperty().multiply(widthRatio));
    //     return col;
    // }

    @Override
    public void open() {
        // TODO
        // ObservableList<Data> data = FXCollections.observableArrayList();

        // for (Entity t : MainApp.schema.getTables()) {
        //     for (Column c : t.getColumns()) {
        //         data.add(new Data(t.name, c.name, "", DD_Type.AN, DD_Nature.E));
        //     }
        // }

        // this.tableView.setItems(data);
    }

    @Override
    public void updateType() {
        // TODO
    }

    @Override
    public void updateStyle() {
        // TODO
    }

    @Override
    public Pane getRoot() {
        return this.root;
    }
}