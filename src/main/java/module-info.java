module com.dbeditor {
    // Modules JavaFX requis
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    
    // Module SQLite JDBC
    // requires java.sql;
    // requires org.xerial.sqlitejdbc;

    requires net.sf.jsqlparser;
    
    // Exporter les packages pour JavaFX
    exports com.dbeditor;
    exports com.dbeditor.controller;
    exports com.dbeditor.model;
    exports com.dbeditor.sql.exporter;
    exports com.dbeditor.sql.parser;
    exports com.dbeditor.util;
    exports com.dbeditor.util.theme;
    // exports com.dbeditor.commands;
    
    // Ouvrir les packages pour la réflexion JavaFX si nécessaire
    opens com.dbeditor to javafx.fxml;
    opens com.dbeditor.controller to javafx.fxml;
}