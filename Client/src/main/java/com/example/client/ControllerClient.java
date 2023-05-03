package com.example.client;

import com.example.api.RequestMessage;
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
    private String prefix = " ";//Поле отвечает за выбор получателя сообщения, меняется при нажатии на кнопку с ником
    private Application uiClient;
    private Stage stage;

    /**
     * Конструктор класса ControllerChat.
     * Получает экземпляр ChatClient и устанавливает его в качестве переменной client.
     */
    public ControllerClient() {
        client = new ChatClient(this);
        client.openConnection();
    }

    /**
     * Обработчик нажатия на кнопку отправки сообщения
     */
    public void sendButtonClick() {
        if (fieldText.getText().isEmpty()) {
            fieldText.requestFocus();
        } else {
            // Формирование сообщения в зависимости от выбранной кнопки с ником клиента
            final String msg = String.format("%s %s",prefix,fieldText.getText().trim());
            RequestMessage requestMessage=RequestMessage.createMessage(msg);
            if (requestMessage!=null) {
                switch (requestMessage.getType()) {
                    case SEND_TO_ALL:
                        addOutgoingMessageForAll(requestMessage.getMessage());
                        client.sendMessage(msg);// Отправка сообщения на сервер
                        break;
                    case SEND_TO_ONE:
                        addOutgoingMessageForOneCustomer(requestMessage.getMessage());
                        client.sendMessage(msg);// Отправка сообщения на сервер
                        break;
                }
            }
            fieldText.clear(); // Очистка текстового поля ввода и установка фокуса на него
            fieldText.requestFocus();
        }
    }

    /**
     * Обработчик выхода из программы, отправляет на сервер команду на завершение работы
     */
    public void exit() {
        client.sendMessage(RequestType.END.getValue());
    }

    /**
     * Добавление исходящего сообщения в окно чата
     */
    public void addOutgoingMessageForAll(String message) {
        areaText.appendText(String.format("->> %s\n", message));
    }

    public void addOutgoingMessageForOneCustomer(String message) {
        areaText.appendText(String.format("-> %s\n", message));
    }

    /**
     * Добавление входящего сообщения в окно чата и обработка сообщения об изменении списка пользователей
     */
    public void addIncomingMessage(ResponseMessage message) {
        areaText.appendText(String.format("From %s %s\n", message.getFromNick(), message.getMessage()));
    }

    /**
     * Метод добавления кнопок.
     * Создает кнопки для отправки сообщений всем пользователям и каждому отдельно.
     * Добавляет их в VBox, очищает VBox перед добавлением.
     *
     * @param nicks массив никнеймов всех клиентов.
     */
    public void addButton(String[] nicks) {
        Platform.runLater(() -> {
            clientsList.getChildren().clear();
            // Создаем группу для кнопок, чтобы была возможность выбирать только одну кнопку за раз
            ToggleGroup toggleGroup = new ToggleGroup();
            ToggleButton buttonAll = new ToggleButton("Отправить всем");// Создаем кнопку для отправки всем пользователям
            buttonAll.setMinWidth(170);
            buttonAll.setOnAction(event -> {
                // Если в чате больше одного пользователя, то задаем префикс сообщения (появляется возможность отправить сообщение всем)
                if (nicks.length > 1) {
                    prefix = String.format("%s", RequestType.SEND_TO_ALL.getValue());
                    fieldText.requestFocus();
                } else {
                    // Иначе выводим сообщение, что в чате нет других пользователей
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText(null);
                    alert.setContentText("В чате пока нет пользователей, кроме вас =(");
                    alert.showAndWait();
                    fieldText.requestFocus();
                }
            });
            //Добавляем кнопку "Отправить всем"
            buttonAll.setToggleGroup(toggleGroup);
            clientsList.getChildren().add(buttonAll);
            fieldText.requestFocus();

            Arrays.sort(nicks);// Сортируем ники по номерам
            // Добавляем кнопки для отправки сообщения каждому пользователю, кроме текущего пользователя
            for (String nick : nicks) {
                if (!nick.equals(client.getNick())) {
                    ToggleButton button = new ToggleButton(nick); // Создаем кнопку и задаем ей ник пользователя
                    button.setMinWidth(170);
                    // При нажатии на кнопку задаем префикс сообщения и фокусируем поле ввода текста
                    button.setOnAction(event -> {
                        prefix = String.format("%s %s", RequestType.SEND_TO_ONE.getValue(), button.getText());
                        fieldText.requestFocus();
                    });
                    button.setToggleGroup(toggleGroup);
                    clientsList.getChildren().add(button);
                    fieldText.requestFocus();
                }
            }
        });
    }

    public void setLabel() {
        // Задаем стиль и размер шрифта для заголовка к списку клиентов
        label.setFont(new Font("Sriracha Regular", 15));
    }

    public ChatClient getClient() {
        return client;
    }

    public void viewWindow() {
        // Сделать все элементы окна чата видимыми
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

    /**
     * Сохраняет ссылки на объекты классов приложения и сцены
     */
    public void takeUIAndControllerClient(Application uiClient, Stage stage) {
        this.uiClient = uiClient;
        this.stage = stage;
    }

    /**
     * Метод передаёт ссылку на объект контроллера окна аутентификации, для того, чтобы в классе ChatClient
     * можно было вызывать методы класса ControllerAuthenticate, обрабатывающие последствия успешной и неуспешной авторизации пользователя
     */

    public void takeControllerAuthenticate(ControllerAuthenticate controllerAuthenticate) {
        client.takeControllerAuthenticate(controllerAuthenticate);
    }
}