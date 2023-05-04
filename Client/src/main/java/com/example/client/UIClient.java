package com.example.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class UIClient extends Application {
    /**
     * Метод start вызывается при запуске приложения и создает сцену для главного окна,
     * загружает FXML-файл, создает контроллер, передает ссылки между объектами и отображает окно.
     * Также создает сцену для окна аутентификации, загружает соответствующий FXML-файл,
     * передает ссылки на объекты, создает новое окно и отображает его.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Создание сцены для главного окна
        FXMLLoader fxmlLoader = new FXMLLoader(UIClient.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();
        ControllerClient controllerClient = fxmlLoader.getController();
        Scene scene = new Scene(root, 650, 400);
        stage.setScene(scene);
        stage.show();
        controllerClient.setLabel();
        controllerClient.setUiClient(this);
        controllerClient.setStage(stage);
        stage.setOnCloseRequest(event -> {
            controllerClient.exit();
        });

        // Создание сцены для окна аутентификации
        FXMLLoader secondaryFxmlLoader = new FXMLLoader(UIClient.class.getResource("authenticate-view.fxml"));
        Parent secondaryRoot = secondaryFxmlLoader.load();
        Stage authenticateStage = new Stage();
        ControllerAuthenticate controllerAuthenticate = secondaryFxmlLoader.getController();
        controllerAuthenticate.setAuthenticateStage(authenticateStage);
        ChatClient client = controllerClient.getClient();
        client.setControllerAuthenticate(controllerAuthenticate);
        controllerAuthenticate.setControllerClient(controllerClient);
        controllerAuthenticate.setStage(stage);
        Scene secondaryScene = new Scene(secondaryRoot, 300, 150);
        authenticateStage.setTitle("Authenticate");
        authenticateStage.setScene(secondaryScene);
        authenticateStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

