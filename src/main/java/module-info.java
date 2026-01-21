module com.dbeditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    // Module SQLite JDBC
    // requires java.sql;
    // requires org.xerial.sqlitejdbc;
    requires net.sf.jsqlparser;
    
    opens com.dbeditor to javafx.fxml;
    opens com.dbeditor.controller to javafx.fxml;

    exports com.dbeditor;
    exports com.dbeditor.controller;
    exports com.dbeditor.model;
    exports com.dbeditor.sql.exporter.file;
    exports com.dbeditor.sql.parser.file;
    exports com.dbeditor.util;
    exports com.dbeditor.util.theme;
    // exports com.dbeditor.commands;
}