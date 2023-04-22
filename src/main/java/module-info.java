module com.example.javafxapp {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.javafxapp to javafx.fxml;
    exports com.example.javafxapp;
    exports com.example.javafxapp.Client;
    opens com.example.javafxapp.Client to javafx.fxml;
}
