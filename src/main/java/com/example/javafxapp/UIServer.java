package com.example.javafxapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class UIServer extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader2 = new FXMLLoader(UIClient.class.getResource("hello-view2.fxml"));
        Scene scene2 = new Scene(fxmlLoader2.load(), 650, 400);
        stage.setTitle("Server");
        stage.setScene(scene2);
        stage.show();
        stage.setOnCloseRequest(event -> event.consume());
    }

    public static void main(String[] args) {
        launch();
    }
}
