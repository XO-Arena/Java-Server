module com.mycompany.java.server.project {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.java.server.project to javafx.fxml;
    exports com.mycompany.java.server.project;
}
