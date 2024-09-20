package com.example.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.IOException;


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
        FXMLLoader loader = new FXMLLoader(UIClient.class.getResource("hello-view.fxml"));
        Parent root = loader.load();
        controllerClient = loader.getController();
        controllerClient.setUiClient(this);

        this.startStage = stage;
        Scene scene = new Scene(root, 650, 400);
        startStage.setScene(scene);
        startStage.show();
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
        registrationStage.setTitle("Registration");
        registrationStage.setScene(thirdScene);
        registrationStage.setOnCloseRequest(event -> {
            controllerAuthenticate.restartTimer();
            authenticateStage.show();
        });

        chatClient = ClientRunner.getContainer().select(ChatClient.class).get();
        chatClient.setControllers(controllerClient, controllerAuthenticate, controllerRegistration);
        chatClient.openConnection();
    }
}

