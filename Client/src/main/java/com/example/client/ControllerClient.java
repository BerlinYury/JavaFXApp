package com.example.client;

import com.example.api.RequestType;
import com.example.api.ResponseMessage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Arrays;

public class ControllerClient {
    @FXML
    private Label label;
    @FXML
    private Button buttonSend;
    @FXML
    private VBox clientsListBox;
    @FXML
    private VBox clientsList;
    @FXML
    private TextArea areaText;
    @FXML
    private TextField fieldText;

    private final ChatClient client;
    private String prefix;//Поле отвечает за выбор получателя сообщения, меняется при нажатии на кнопку с ником
    private Application uiClient;
    private Stage stage;
    private String toNick;


    public ControllerClient() {
        client = new ChatClient(this);
        client.openConnection();
    }

    public void sendButtonClick() {
        if (fieldText.getText().isEmpty()) {
            fieldText.requestFocus();
            return;
        }
        if (prefix == null) {
            fieldText.clear();
            fieldText.requestFocus();
            return;
        }
        RequestType requestType = RequestType.getRequestType(prefix);
        String msgWithoutPrefix = fieldText.getText().trim();
        if (requestType == null) {
            return;
            //TODO: Добавть логирование
        }
            switch (requestType) {
                case SEND_TO_ALL:
                    String msgToAll = String.format("%s %s", prefix, msgWithoutPrefix);
                    addOutgoingMessageForAll(msgWithoutPrefix);
                    client.sendMessage(msgToAll);
                    break;
                case SEND_TO_ONE:
                    String msgToOne = String.format("%s %s %s", prefix, toNick, msgWithoutPrefix);
                    addOutgoingMessageForOneCustomer(msgWithoutPrefix);
                    client.sendMessage(msgToOne);
                    break;
            }
            fieldText.clear();
            fieldText.requestFocus();
        }


    public void exit() {
        client.sendMessage(RequestType.END.getValue());
    }

    public void addOutgoingMessageForAll(String message) {
        areaText.appendText(String.format("->> %s\n", message));
    }

    public void addOutgoingMessageForOneCustomer(String message) {
        areaText.appendText(String.format("-> %s\n", message));
    }

    public void addIncomingMessage(ResponseMessage message) {
        areaText.appendText(String.format("From %s %s\n", message.getFromNick(), message.getMessage()));
    }

    /**
     * Метод добавления кнопок.
     * Создает кнопки для отправки сообщений всем пользователям сразу и каждому отдельно.
     *
     * @param nicks массив никнеймов всех клиентов.
     */
    public void addButtons(String[] nicks) {
        Platform.runLater(() -> {
            clientsList.getChildren().clear();
            // Создаем группу для кнопок, чтобы была возможность выбирать только одну кнопку за раз
            ToggleGroup toggleGroup = new ToggleGroup();
            addButtonToAll(nicks.length, toggleGroup);
            addButtonToOneCustomer(nicks, toggleGroup);
            fieldText.requestFocus();
        });
    }

    private void addButtonToAll(int arrNicksLength, ToggleGroup toggleGroup) {
        ToggleButton buttonAll = new ToggleButton("Отправить всем");
        buttonAll.setMinWidth(170);
        buttonAll.setOnAction(event -> {
            if (arrNicksLength > 1) {
                // Если в чате больше одного пользователя, то задаем префикс сообщения (появляется возможность отправить сообщение всем)
                prefix = String.format("%s", RequestType.SEND_TO_ALL.getValue());
                fieldText.requestFocus();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText(null);
                alert.setContentText("В чате пока нет пользователей, кроме вас =(");
                alert.showAndWait();
                fieldText.requestFocus();
                toggleGroup.selectToggle(null);
            }
        });
        buttonAll.setToggleGroup(toggleGroup);
        clientsList.getChildren().add(buttonAll);
    }

    private void addButtonToOneCustomer(String[] nicks, ToggleGroup toggleGroup) {
        Arrays.sort(nicks);
        for (String nick : nicks) {
            if (nick.equals(client.getNick())) {
                continue;
            }
            ToggleButton button = new ToggleButton(nick);
            button.setMinWidth(170);
            // При нажатии на кнопку задаем префикс сообщения (появляется возможность отправить сообщение конкретному пользователю)
            button.setOnAction(event -> {
                prefix = String.format("%s", RequestType.SEND_TO_ONE.getValue());
                this.toNick=button.getText();//Ник получателя сообщения берём с выбранной кнопи.
                fieldText.requestFocus();
            });
            button.setToggleGroup(toggleGroup);
            clientsList.getChildren().add(button);
        }
    }


    public void setLabel() {
        label.setFont(new Font("Sriracha Regular", 15));
    }

    public ChatClient getClient() {
        return client;
    }

    public void viewWindow() {
        areaText.setVisible(true);
        fieldText.setVisible(true);
        buttonSend.setVisible(true);
        clientsListBox.setVisible(true);
    }

    /**
     * Метод перезапускает окно клиента, возвращаая нас на окно авторизации.
     * Отключает текущее подключение клиента через метод exit()
     */
    public void logout() {
        try {
            uiClient.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        exit();
    }

    public void setUiClient(Application uiClient) {
        this.uiClient = uiClient;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}