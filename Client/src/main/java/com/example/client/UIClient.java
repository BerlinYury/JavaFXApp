package com.example.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class UIClient extends Application {

    private ControllerClient controllerClient; // сделайте поле, чтобы иметь доступ к контроллеру
    private ControllerAuthenticate controllerAuthenticate; // сделайте поле, чтобы иметь доступ к контроллеру

    /**
     Метод start вызывается при запуске приложения и создает сцену для главного окна,
     загружает FXML-файл, создает контроллер, передает ссылки между объектами и отображает окно.
     Также создает сцену для окна аутентификации, загружает соответствующий FXML-файл,
     передает ссылки на объекты, создает новое окно и отображает его.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Создание сцены для главного окна
        FXMLLoader fxmlLoader = new FXMLLoader(UIClient.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();
        controllerClient = fxmlLoader.getController(); // получаем контроллер из загруженного FXML
        Scene scene = new Scene(root, 650, 400);
        stage.setScene(scene);
        stage.show();
        // Установка метки на форме главного окна
        controllerClient.setLabel();
        // Передача ссылок на объекты между контроллером главного окна и UI
        controllerClient.takeUIAndControllerClient(this, stage);
        // Обработчик события закрытия главного окна
        stage.setOnCloseRequest(event -> {
            controllerClient.exit(); // вызываем метод exit() из контроллера
        });

        // Создание сцены для окна аутентификации
        FXMLLoader secondaryFxmlLoader = new FXMLLoader(UIClient.class.getResource("authenticate-view.fxml"));
        Parent secondaryRoot = secondaryFxmlLoader.load();
        Stage authenticateStage = new Stage();
        controllerAuthenticate = secondaryFxmlLoader.getController();
        controllerAuthenticate.setStage(authenticateStage); // передаем ссылку на Stage
        // Передача ссылок на объекты между контроллером главного окна и контроллером окна аутентификации
        controllerAuthenticate.takeController(controllerClient, stage);
        Scene secondaryScene = new Scene(secondaryRoot, 300, 150);
        authenticateStage.setTitle("Authenticate");
        authenticateStage.setScene(secondaryScene);
        authenticateStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
