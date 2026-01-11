module com.mycompany.java.server.project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.sql;
    requires com.google.gson;
    
    opens com.mycompany.java.server.project to javafx.fxml;
    opens data to com.google.gson;
    opens enums to com.google.gson;
    opens dto to com.google.gson;
    opens models to com.google.gson;
    exports com.mycompany.java.server.project;
}
