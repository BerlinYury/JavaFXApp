package com.example.client;

import com.example.api.MessageBox;
import com.example.api.MessageType;
import com.example.api.RequestType;
import com.example.api.ResponseMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;


import static java.util.Objects.*;

@Slf4j
@NoArgsConstructor
public class ControllerClient extends Controller implements IControllerClient {
    @FXML
    private Label label;
    @FXML
    private Button buttonSend;
    @FXML
    private VBox clientsListBox;
    @FXML
    private VBox clientsList;
    @FXML
    private VBox messagesContainer;
    @FXML
    private TextField fieldText;
    @FXML
    private ScrollPane scrollPane;
    private String prefix;//Поле отвечает за выбор получателя сообщения, меняется при нажатии на кнопку с ником
    private String toNick;
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Setter
    private UIClient uiClient;

    @FXML
    private void sendButtonClick() {
        if (fieldText.getText().isEmpty()) {
            fieldText.requestFocus();
            return;
        }
        if (isNull(prefix)) {
            prefixIsEmptyError();
            fieldText.requestFocus();
            return;
        }
        RequestType requestType = RequestType.getRequestType(prefix);
        String msgWithoutPrefix = fieldText.getText().trim();
        if (isNull(requestType)) {
            String messageError = "requestType == null";
            log.error(messageError);
            throw new NullPointerException(messageError);
        }
        switch (requestType) {
            case SEND_TO_ALL -> {
                MessageBox messageBox = new MessageBox(MessageType.OUTGOING_MESSAGE_FOR_ALL,
                        LocalDateTime.now(), String.format("->> %s\n\n", msgWithoutPrefix));
                uiClient.getChatClient().getMessageSession().add(messageBox);
                addMessage(messageBox);
                uiClient.getChatClient().sendMessage(String.format("%s %s", prefix, msgWithoutPrefix));
            }
            case SEND_TO_ONE -> {
                MessageBox messageBox = new MessageBox(MessageType.OUTGOING_MESSAGE_FOR_ONE_CUSTOMER,
                        LocalDateTime.now(),
                        String.format("-> %s %s\n\n", toNick, msgWithoutPrefix));
                addMessage(messageBox);
                uiClient.getChatClient().getMessageSession().add(messageBox);
                uiClient.getChatClient().sendMessage(String.format("%s %s %s", prefix, toNick, msgWithoutPrefix));
            }
        }
        fieldText.clear();
        fieldText.requestFocus();
    }

    @Override
    public void addIncomingMessage(ResponseMessage message) {
        MessageBox messageBox;
        switch (message.getType()) {
            case USER_ON -> messageBox = new MessageBox(MessageType.INFORMATION_MESSAGE,
                    LocalDateTime.now(), String.format("Пользователь %s присоединился\n\n", message.getNick()));

            case USER_OFF -> messageBox = new MessageBox(MessageType.INFORMATION_MESSAGE,
                    LocalDateTime.now(), String.format("Пользователь %s вышел из чата\n\n", message.getNick()));

            case RESPONSE -> {
                messageBox = new MessageBox(MessageType.INCOMING_MESSAGE,
                        LocalDateTime.now(), String.format("%s: %s\n\n", message.getFromNick(), message.getMessage()));
                uiClient.getChatClient().getMessageSession().add(messageBox);
            }
            default -> messageBox = null;
        }
        addMessage(messageBox);
    }

    @Override
    public void appendOldMessages(List<MessageBox> oldMessageSession) {
        String targetDate = "";
        for (MessageBox messageBox : oldMessageSession) {
            String nextDate = messageBox.getDateTime().format(dateFormat);
            if (!targetDate.equals(nextDate)) {
                targetDate = nextDate;
                addMessage(new MessageBox(MessageType.DATE_OF_MESSAGE, messageBox.getDateTime(), targetDate));
            }
            addMessage(messageBox);
        }
        String welcomeWithDateMessage = String.format("Добро пожаловать в чат!\n            %s\n\n",
                LocalDateTime.now().format(dateFormat));
        addMessage(new MessageBox(MessageType.DATE_OF_MESSAGE, LocalDateTime.now(), welcomeWithDateMessage));
    }

    @Override
    public void setLabel() {
        label.setFont(new Font("Sriracha Regular", 15));
    }

    @FXML
    public void logout() {
        try {
            exit();
            uiClient.getStartStage().close();
            uiClient.start(uiClient.getStartStage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exit() {
        try {
            ChatClient chatClient = uiClient.getChatClient();
            if (nonNull(chatClient.getNick())) {
                chatClient.serializeMessages();
            }
            chatClient.sendMessage(RequestType.END.getValue());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
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

    private void addMessage(MessageBox messageBox) {
        Platform.runLater(() -> {
            Label messageLabel = new Label(messageBox.getMessage());
            messageLabel.setWrapText(true);
            HBox messageContainer = new HBox();
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            switch (messageBox.getType()) {
                case INCOMING_MESSAGE -> messageContainer.getChildren().addAll(messageLabel, spacer);
                case INFORMATION_MESSAGE ->
                        messageContainer.getChildren().addAll(messageLabel, spacer); // Входящие слева
                case OUTGOING_MESSAGE_FOR_ALL, OUTGOING_MESSAGE_FOR_ONE_CUSTOMER ->
                        messageContainer.getChildren().addAll(spacer, messageLabel); // Исходящие справа
                case DATE_OF_MESSAGE -> {
                    messageContainer.setAlignment(Pos.CENTER); // Центрирование
                    messageContainer.getChildren().add(messageLabel);
                }
            }
            messagesContainer.getChildren().add(messageContainer);
        });
        // Прокрутка вниз после добавления сообщения
        messagesContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            scrollPane.setVvalue(1.0);
        });
    }

    private void prefixIsEmptyError() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Упс!");
        alert.setHeaderText(null);
        alert.setContentText("Получатель не выбран");
        alert.showAndWait();
    }

    private void addButtonToAll(int arrNicksLength, ToggleGroup toggleGroup) {
        ToggleButton buttonAll = new ToggleButton("Отправить всем");
        buttonAll.getStyleClass().add("toggle-button-client");
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
            if (nick.equals(uiClient.getChatClient().getNick())) {
                continue;
            }
            ToggleButton button = new ToggleButton(nick);
            button.getStyleClass().add("toggle-button-client");
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

}