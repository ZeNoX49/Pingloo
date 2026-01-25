module com.dbeditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    
    requires net.sf.jsqlparser;
    requires java.sql;
    
    opens com.dbeditor to javafx.fxml;
    opens com.dbeditor.controller to javafx.fxml;
    opens com.dbeditor.controller.parameter to javafx.fxml;
    opens com.dbeditor.controller.view to javafx.fxml;

    exports com.dbeditor;
    exports com.dbeditor.controller;
    exports com.dbeditor.controller.parameter;
    exports com.dbeditor.controller.view;
    exports com.dbeditor.model;
    exports com.dbeditor.sql.db;
    exports com.dbeditor.sql.file.exporter;
    exports com.dbeditor.sql.file.parser;
    exports com.dbeditor.theme;
    exports com.dbeditor.util;
    // exports com.dbeditor.commands;
}