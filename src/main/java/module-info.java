module com.dbeditor {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    opens com.dbeditor to javafx.fxml;

    exports com.dbeditor;
}