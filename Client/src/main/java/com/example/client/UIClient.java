package com.example.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;


@NoArgsConstructor
@Getter
public class UIClient extends Application {
    ChatClient chatClient;

    public static void go(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage startStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(UIClient.class.getResource("start-view.fxml"));
        Parent root = loader.load();
        ControllerClient controllerClient = loader.getController();

        Scene scene = new Scene(root, 800, 537);
        scene.getStylesheets().add(Objects.requireNonNull(UIClient.class.getResource("style.css")).toExternalForm());
        startStage.setScene(scene);
        controllerClient.setUnitsListLabel();

        FXMLLoader secondaryFxmlLoader = new FXMLLoader(UIClient.class.getResource("authenticate-view.fxml"));
        Parent secondaryRoot = secondaryFxmlLoader.load();
        ControllerAuthenticate controllerAuthenticate = secondaryFxmlLoader.getController();

        Stage authenticateStage = new Stage();
        Scene secondaryScene = new Scene(secondaryRoot, 320, 170);
        secondaryScene.getStylesheets().add(Objects.requireNonNull(UIClient.class.getResource("style.css")).toExternalForm());
        authenticateStage.setTitle("Authenticate");
        authenticateStage.setScene(secondaryScene);
        authenticateStage.show();

        FXMLLoader thirdFxmlLoader = new FXMLLoader(UIClient.class.getResource("registration-view.fxml"));
        Parent thirdRoot = thirdFxmlLoader.load();
        ControllerRegistrationPerson controllerRegistrationPerson = thirdFxmlLoader.getController();

        Stage registrationStage = new Stage();
        Scene thirdScene = new Scene(thirdRoot, 320, 400);
        thirdScene.getStylesheets().add(Objects.requireNonNull(UIClient.class.getResource("style.css")).toExternalForm());
        registrationStage.setTitle("Registration");
        registrationStage.setScene(thirdScene);

        startStage.setOnCloseRequest(event -> {
            controllerAuthenticate.offTimer();
            controllerClient.closeAllWindows();
        });
        authenticateStage.setOnCloseRequest(event -> {
            controllerAuthenticate.offTimer();
            controllerAuthenticate.closeAllWindows();
        });
        registrationStage.setOnCloseRequest(event -> {
            controllerAuthenticate.restartTimer();
            authenticateStage.show();
        });
        try {
            chatClient = new ChatClient(controllerClient, controllerAuthenticate, controllerRegistrationPerson);
            chatClient.connect(); // Устанавливаем соединение
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        Controller.setUiClient(this, chatClient, startStage,
                authenticateStage, registrationStage, controllerClient,
                controllerAuthenticate, controllerRegistrationPerson);
    }

    public void startCreateChatStage() {
        FXMLLoader fifthFxmlLoader = new FXMLLoader(UIClient.class.getResource("create-chat-view.fxml"));
        Parent fifthRoot;
        try {
            fifthRoot = fifthFxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ControllerCreateChat controllerCreateChat = fifthFxmlLoader.getController();

        Stage createChatStage = new Stage();
        Scene fifthScene = new Scene(fifthRoot, 500, 428);
        fifthScene.getStylesheets().add(Objects.requireNonNull(UIClient.class.getResource("style.css")).toExternalForm());
        createChatStage.setTitle("Create new Chat");
        createChatStage.setScene(fifthScene);

        Controller.setCreateChat(controllerCreateChat, createChatStage);
        chatClient.setControllerCreateChat(controllerCreateChat);
        createChatStage.show();
    }
    public void startCreateGroupStage(){
        FXMLLoader fourthFxmlLoader = new FXMLLoader(UIClient.class.getResource("create-group-view.fxml"));
        Parent fourthRoot;
        try {
            fourthRoot = fourthFxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ControllerCreateGroup controllerCreateGroup = fourthFxmlLoader.getController();

        Stage createGroupStage = new Stage();
        Scene fourthScene = new Scene(fourthRoot, 500, 428);
        fourthScene.getStylesheets().add(Objects.requireNonNull(UIClient.class.getResource("style.css")).toExternalForm());
        createGroupStage.setTitle("Create new Group");
        createGroupStage.setScene(fourthScene);

        Controller.setControllerCreateGroup(controllerCreateGroup,createGroupStage);
        chatClient.setControllerCreateGroup(controllerCreateGroup);
        createGroupStage.show();
    }
}

