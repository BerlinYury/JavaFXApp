package com.example.client;

import com.example.api.RequestType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ControllerAuthenticate   {
    @FXML
    private TextField login;
    @FXML
    private TextField password;

    private Stage authenticateStage; // окно аутентификации
    private Stage stage; // главное окно
    private ControllerClient controllerClient; // экземпляр контроллера клиента
    private boolean isAuth=false;// флаг, указывающий, авторизован ли пользователь
    private final int SECOND_TO_MILLIS_MULTIPLIER = 1000;// множитель для перевода секунд в миллисекунды


    public ControllerAuthenticate() {
       timeLimit();
    }

    /** Метод для запуска таймера, который закроет окна, если пользователь не авторизовался в течение 120 секунд
     */
    private void timeLimit() {
        Thread timer = new Thread(() -> {
            try {
                Thread.sleep(120*SECOND_TO_MILLIS_MULTIPLIER);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!isAuth) {
                closeAllWindows();
            }
        });
        timer.setDaemon(true);
        timer.start();
    }

    /** Метод для закрытия всех окон
     */
    private void closeAllWindows() {
        Platform.runLater(() -> {
            controllerClient.exit();
            authenticateStage.close();
            stage.close();
        });
    }

    /**
     * Обрабатывает нажатие на кнопку "Авторизоваться"
     * Если логин и пароль не пустые, отправляет сообщение на сервер для авторизации
     * Если одно из полей пустое, выводит сообщение об ошибке
     * @param actionEvent - событие нажатия на кнопку
     */
    public void sendButtonAuth(ActionEvent actionEvent) {
        String loginText = login.getText().trim();
        String passwordText = password.getText().trim();
        if (loginText.isEmpty() || passwordText.isEmpty()) {
            login.clear();
            password.clear();
            login.requestFocus();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Пустые поля");
            alert.setHeaderText(null);
            alert.setContentText("Пожалуйста, заполните поля Логин и Пароль");
            alert.showAndWait();
        }else {
            String msgAuth = String.format("%s %s %s", RequestType.AUTH.getValue(), loginText, passwordText);
            controllerClient.getClient().sendMessage(msgAuth);
        }
    }

    /**
     * Метод вызывается при успешной авторизации
     * Открывает главное окно, закрывает окно авторизации, устанавливает флаг авторизации
     */
    public void onSuccess() {
        Platform.runLater(() -> {
        controllerClient.viewWindow();
        stage.setTitle(controllerClient.getClient().getNick());
        authenticateStage.close();
        isAuth=true;
        });
    }

    /**
     * Метод вызывается при ошибке авторизации
     * Очищает поля логина и пароля, выводит сообщение об ошибке
     */
    public void onError() {
        Platform.runLater(() -> {
        login.clear();
        password.clear();
        login.requestFocus();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка авторизации");
        alert.setHeaderText(null);
        alert.setContentText("Вы ввели неверный логин или пароль, попробуйте снова =) ");
        alert.showAndWait();
        });
    }

    /**
     Показывает сообщение об ошибке, если пользователь с введенными учетными данными уже зарегистрирован в чате.
     Очищает поля для ввода логина и пароля и устанавливает фокус на поле для ввода логина.
     */
    public void onBusy() {
        Platform.runLater(() -> {
            login.clear();
            password.clear();
            login.requestFocus();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("nick уже занят");
            alert.setHeaderText(null);
            alert.setContentText("Пользователь с такими учётными данными уже есть в чате");
            alert.showAndWait();
        });
    }

    /**
     Устанавливает фокус на поле для ввода пароля.
     */
    public void nextField() {
        password.requestFocus();
    }

    /**
     Обрабатывает нажатие на кнопку "Отправить" на экране аутентификации.
     */
    public void nextField1(ActionEvent actionEvent) {
        sendButtonAuth(actionEvent);
    }

    /**
     Получает окно аутентификации.
     @param authenticateStage окно аутентификации
     */
    public void setStage(Stage authenticateStage) {
        this.authenticateStage = authenticateStage;
    }

    /**

     Получает экземпляр контроллера клиента и главное окно приложения.
     @param controllerClient экземпляр контроллера клиента
     @param stage главное окно приложения
     */
    public void takeController(ControllerClient controllerClient, Stage stage) {
        this.controllerClient = controllerClient;
        this.stage = stage;
        controllerClient.takeControllerAuthenticate(this);
    }
}

