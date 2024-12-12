package com.example.server;


import javafx.application.Platform;
import javafx.fxml.FXML;
import lombok.Setter;

public class ControllerServer {
    @Setter
    private ChatServer chatServer;

    @FXML
    private void stopButtonClick() {
        chatServer.stopServer(); // Останавливаем сервер
        Platform.exit(); // Закрываем JavaFX приложение
    }
}
