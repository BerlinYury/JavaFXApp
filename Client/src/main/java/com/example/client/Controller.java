package com.example.client;

import com.example.api.Person;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Controller {
    protected static UIClient uiClient;
    protected static ChatClient chatClient;
    protected static ConcurrentHashMap<String, Correspondence> correspondenceMap;
    protected static Stage startStage;
    protected static Stage authenticateStage;
    protected static Stage registrationStage;
    protected static Stage createGroupStage;
    protected static Stage createChatStage;
    protected static ControllerClient controllerClient;
    protected static ControllerAuthenticate controllerAuthenticate;
    protected static ControllerRegistrationPerson controllerRegistrationPerson;
    protected static ControllerCreateGroup controllerCreateGroup;
    protected static ControllerCreateChat controllerCreateChat;
    protected static Person myPerson;

    public static void setCreateChat(ControllerCreateChat controllerCreateChat, Stage createChatStage) {
        Controller.controllerCreateChat = controllerCreateChat;
        Controller.createChatStage = createChatStage;
    }

    public static void setControllerCreateGroup(ControllerCreateGroup controllerCreateGroup, Stage createGroupStage) {
        Controller.controllerCreateGroup = controllerCreateGroup;
        Controller.createGroupStage = createGroupStage;
    }

    public static void setUiClient(
            UIClient uiClient,
            ChatClient chatClient,
            Stage startStage,
            Stage authenticateStage,
            Stage registrationStage,
            ControllerClient controllerClient,
            ControllerAuthenticate controllerAuthenticate,
            ControllerRegistrationPerson controllerRegistrationPerson
    ) {
        Controller.correspondenceMap = chatClient.getCorrespondenceMap();
        Controller.uiClient = uiClient;
        Controller.chatClient = chatClient;
        Controller.startStage = startStage;
        Controller.authenticateStage = authenticateStage;
        Controller.registrationStage = registrationStage;
        Controller.controllerClient = controllerClient;
        Controller.controllerAuthenticate = controllerAuthenticate;
        Controller.controllerRegistrationPerson = controllerRegistrationPerson;
    }

    public abstract void clearFields();

    public void closeAllWindows() {
        Platform.runLater(() -> {
            if (Objects.nonNull(controllerClient)) {
                controllerClient.exit();
            }
            if (Objects.nonNull(authenticateStage)) {
                authenticateStage.close();
            }
            if (Objects.nonNull(createChatStage)) {
                createChatStage.close();
            }
            if (Objects.nonNull(createGroupStage)) {
                createGroupStage.close();
            }
            if (Objects.nonNull(startStage)) {
                startStage.close();
            }
            ThreadManagerClient.getInstance().shutdownMyExecutorService();
            ThreadManagerClient.getInstance().shutdownMyScheduledExecutorService();
        });
    }

    public void showInformationMessage(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("style.css")).toExternalForm()
        );
        alert.showAndWait();
    }
}
