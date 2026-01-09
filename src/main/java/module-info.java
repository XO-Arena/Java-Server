module com.mycompany.java.server.project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.sql;
    requires com.google.gson;
    opens com.mycompany.java.server.project to javafx.fxml;
    exports com.mycompany.java.server.project;
}
