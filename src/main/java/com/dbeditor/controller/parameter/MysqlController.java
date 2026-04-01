package com.dbeditor.controller.parameter;

import com.dbeditor.MainApp;
import com.dbeditor.sql.DbType;
import com.dbeditor.util.DbManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MysqlController {
    DbManager D_M = DbManager.getInstance();

    @FXML private TextField tfHost, tfPassword, tfPort, tfUser;
    @FXML private VBox vboxBdd;

    @FXML
    void initialize() {
        this.tfHost.setText(D_M.getSqlDb(DbType.MySql).dbHost);
        this.tfUser.setText(D_M.getSqlDb(DbType.MySql).dbUser);
        this.tfPassword.setText(D_M.getSqlDb(DbType.MySql).dbPassword);
        this.tfPort.setText(D_M.getSqlDb(DbType.MySql).dbPort);

        for(String name :  D_M.getSqlTypeDatabases(DbType.MySql)) {
            this.createBddUtil(name);
        }
    }

    @FXML
    void addBdd(ActionEvent event) {
        this.createBddUtil("");
    }

    private void createBddUtil(String name) {
        HBox hbox = new HBox();
        hbox.setSpacing(5);

        TextField tf = new TextField(name);
        tf.textProperty().addListener((observable, oldValue, newValue) -> {
            // supprime l'ancien nom s'il existe
            if (!oldValue.isEmpty()) {
                D_M.getSqlTypeDatabases(DbType.MySql).remove(oldValue);
            }
            // ajoute le nouveau si ce n'est pas vide
            if (!newValue.isEmpty() && !D_M.getSqlTypeDatabases(DbType.MySql).contains(newValue)) {
                D_M.getSqlTypeDatabases(DbType.MySql).add(newValue);
            }
        });

        Button btn = new Button();
        btn.setStyle("-fx-background-color: transparent");

        ImageView img = new ImageView(new Image(MainApp.class.getResource("/img/delete.png").toString(), 20, 20, true, true));
        btn.setGraphic(img);

        btn.setOnAction(e -> {
            vboxBdd.getChildren().remove(hbox);
            String n = tf.getText();
            if (!n.isEmpty()) {
                D_M.getSqlTypeDatabases(DbType.MySql).remove(n);
            }
        });

        hbox.getChildren().addAll(tf, btn);

        this.vboxBdd.getChildren().add(hbox);
    }

}
