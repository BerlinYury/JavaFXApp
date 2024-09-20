package com.example.client;

import com.example.api.RequestType;
import com.example.api.ResponseMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;

@Slf4j
@NoArgsConstructor
public class ControllerClient extends Controller implements IControllerClient {
    public Button ooo;
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
    private VBox messagesBox;
    @FXML
    private TextField fieldText;
    private String prefix;//Поле отвечает за выбор получателя сообщения, меняется при нажатии на кнопку с ником
    private String toNick;

    @Setter
    private UIClient uiClient;

    @FXML
    private void sendButtonClick() {
        if (fieldText.getText().isEmpty()) {
            fieldText.requestFocus();
            return;
        }
        if (prefix == null) {
            prefixIsEmptyError();
            fieldText.requestFocus();
            return;
        }
        RequestType requestType = RequestType.getRequestType(prefix);
        String msgWithoutPrefix = fieldText.getText().trim();
        if (requestType == null) {
            log.error("requestType == null");
            return;
        }
        switch (requestType) {
            case SEND_TO_ALL -> {
                String msgToAll = String.format("%s %s", prefix, msgWithoutPrefix);
                addOutgoingMessageForAll(msgWithoutPrefix);
                uiClient.getChatClient().sendMessage(msgToAll);
            }
            case SEND_TO_ONE -> {
                String msgToOne = String.format("%s %s %s", prefix, toNick, msgWithoutPrefix);
                addOutgoingMessageForOneCustomer(msgWithoutPrefix);
                uiClient.getChatClient().sendMessage(msgToOne);
            }
        }
        fieldText.clear();
        fieldText.requestFocus();
    }

    @Override
    public void addIncomingMessage(ResponseMessage message) {
        areaText.appendText(String.format("From %s %s %s\n", message.getFromNick(), message.getMessage(),createDate("HH:mm")));
    }

    @Override
    public void appendOldMessages(String oldMessages) {
        if (oldMessages == null) {
            return;
        }
        areaText.appendText(String.format("%s\n%s\nДобро пожаловать в чат!\n", oldMessages, createDate("dd.MM.yyyy")));
        //TODO добавить проверку даты, если дата одинаковая не добавлять новую(String pattern)
    }

    @Override
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

    @Override
    public void viewWindow() {
        areaText.setVisible(true);
        fieldText.setVisible(true);
        buttonSend.setVisible(true);
        clientsListBox.setVisible(true);
    }

    @Override
    public void setLabel() {
        label.setFont(new Font("Sriracha Regular", 15));
    }

    @Override
    public void exit() {
        uiClient.getChatClient().sendMessage(String.format("%s %s", RequestType.RETENTION.getValue(), areaText.getText()));
        uiClient.getChatClient().sendMessage(RequestType.END.getValue());
    }

    /**
     * Метод перезапускает окно клиента, возвращая нас на окно авторизации.
     * Отключает текущее подключение клиента через метод exit()
     */
    @FXML
    public void logout() {
        exit();
        try {
            uiClient.start(uiClient.getStartStage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addOutgoingMessageForAll(String message) {
        areaText.appendText(String.format("->> %s %s\n", message,createDate("HH:mm")));
    }

    private void addOutgoingMessageForOneCustomer(String message) {
        areaText.appendText(String.format("->%s %s %s\n", toNick, message,createDate("HH:mm")));
    }

    private void addButtonToAll(int arrNicksLength, ToggleGroup toggleGroup) {
        ToggleButton buttonAll = new ToggleButton("Отправить всем");
        buttonAll.setMinWidth(170);
        buttonAll.setOnAction(event -> {
            if (arrNicksLength > 1) {
                // Если в чате больше одного пользователя, то задаем префикс сообщения (появляется возможность
                // отправить сообщение всем)
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
            if (nick.equals( uiClient.getChatClient().getNick())) {
                continue;
            }
            ToggleButton button = new ToggleButton(nick);
            button.setMinWidth(170);
            // При нажатии на кнопку задаем префикс сообщения (появляется возможность отправить сообщение конкретному
            // пользователю)
            button.setOnAction(event -> {
                prefix = String.format("%s", RequestType.SEND_TO_ONE.getValue());
                this.toNick = button.getText();//Ник получателя сообщения берём с выбранной кнопки.
                fieldText.requestFocus();
            });
            button.setToggleGroup(toggleGroup);
            clientsList.getChildren().add(button);
        }
    }

    private String createDate(String pattern) {
        return new SimpleDateFormat(pattern)
                .format(new Date(System.currentTimeMillis()));
    }
    private void prefixIsEmptyError() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Упс!");
        alert.setHeaderText(null);
        alert.setContentText("Получатель не выбран");
        alert.showAndWait();
    }

}