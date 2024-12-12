package com.example.server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.Objects;


public class UIServer extends Application {
    @Getter
    private Stage stage;

    public static void startFXWindow(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        FXMLLoader loader = new FXMLLoader(UIServer.class.getResource("server-view.fxml"));
        Parent root = loader.load();
        ControllerServer controller = loader.getController();
        ChatServer chatServer = ServerRunner.getChatServer();
        controller.setChatServer(chatServer);

        Scene scene = new Scene(root, 200, 150);
        scene.getStylesheets().add(Objects.requireNonNull(UIServer.class.getResource("style.css")).toExternalForm());
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> {
            chatServer.stopServer(); // Останавливаем сервер
            Platform.exit(); // Завершаем приложение
        });
    }
}
