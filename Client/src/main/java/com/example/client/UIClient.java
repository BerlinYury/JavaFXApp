package com.example.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Objects;


@NoArgsConstructor
@Getter
public class UIClient extends Application implements IUIClient {
    private ControllerClient controllerClient;
    private ControllerAuthenticate controllerAuthenticate;
    private ControllerRegistration controllerRegistration;
    private ChatClient chatClient;
    private Stage startStage;
    private Stage authenticateStage;
    private Stage registrationStage;

    public static void go(String[] args) {
        launch(args);
    }

    /**
     * Метод start вызывается при запуске приложения и создает сцену для главного окна,
     * загружает FXML-файл, создает контроллер, передает ссылки между объектами и отображает окно.
     * Также создает сцену для окна аутентификации, загружает соответствующий FXML-файл,
     * передает ссылки на объекты, создает новое окно и отображает его.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Создание сцены для главного окна
        FXMLLoader loader = new FXMLLoader(UIClient.class.getResource("start-view.fxml"));
        Parent root = loader.load();
        controllerClient = loader.getController();
        controllerClient.setUiClient(this);

        this.startStage = stage;
        Scene scene = new Scene(root, 800, 500);
        scene.getStylesheets().add(Objects.requireNonNull(UIClient.class.getResource("style.css")).toExternalForm());
        startStage.setScene(scene);
        controllerClient.setLabel();
        startStage.setOnCloseRequest(event -> {
            controllerAuthenticate.offTimer();
            controllerAuthenticate.closeAllWindows();
        });

        // Создание сцены для окна аутентификации
        FXMLLoader secondaryFxmlLoader = new FXMLLoader(UIClient.class.getResource("authenticate-view.fxml"));
        Parent secondaryRoot = secondaryFxmlLoader.load();
        controllerAuthenticate = secondaryFxmlLoader.getController();
        controllerAuthenticate.setUiClient(this);

        authenticateStage = new Stage();
        Scene secondaryScene = new Scene(secondaryRoot, 320, 170);
        secondaryScene.getStylesheets().add(Objects.requireNonNull(UIClient.class.getResource("style.css")).toExternalForm());
        authenticateStage.setTitle("Authenticate");
        authenticateStage.setScene(secondaryScene);
        authenticateStage.show();
        authenticateStage.setOnCloseRequest(event -> {
            controllerAuthenticate.offTimer();
            controllerAuthenticate.closeAllWindows();
        });

        // Создание сцены для окна регистрации
        FXMLLoader thirdFxmlLoader = new FXMLLoader(UIClient.class.getResource("registration-view.fxml"));
        Parent thirdRoot = thirdFxmlLoader.load();
        controllerRegistration = thirdFxmlLoader.getController();
        controllerRegistration.setUiClient(this);

        registrationStage = new Stage();
        Scene thirdScene = new Scene(thirdRoot, 320, 400);
        thirdScene.getStylesheets().add(Objects.requireNonNull(UIClient.class.getResource("style.css")).toExternalForm());
        registrationStage.setTitle("Registration");
        registrationStage.setScene(thirdScene);
        registrationStage.setOnCloseRequest(event -> {
            controllerAuthenticate.restartTimer();
            authenticateStage.show();
        });

        chatClient = new ChatClient(controllerClient, controllerAuthenticate, controllerRegistration);
        chatClient.openConnection();
    }
}

