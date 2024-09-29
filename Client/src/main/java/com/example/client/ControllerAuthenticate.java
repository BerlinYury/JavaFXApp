package com.example.client;

import com.example.api.RequestType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import lombok.Setter;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class ControllerAuthenticate extends Controller implements IControllerAuthenticate{
    @FXML
    private TextField login;
    @FXML
    private TextField password;

    private boolean isAuth = false;// флаг, указывающий, авторизован ли пользователь
    private ScheduledFuture<?> scheduledFuture;

    @Setter
    private UIClient uiClient;

    public ControllerAuthenticate() {
        ThreadManagerClient.getInstance().getExecutorService().execute(this::timeLimit);
    }


    /**
     * Обрабатывает нажатие на кнопку "Авторизоваться"
     * Если логин и пароль не пустые, отправляет сообщение на сервер для авторизации
     * Если одно из полей пустое, выводит сообщение об ошибке
     */
    @FXML
    public void sendButtonEnter() {
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
        } else {
            String msgAuth = String.format("%s %s %s", RequestType.AUTH.getValue(), loginText, passwordText);
            uiClient.getChatClient().sendMessage(msgAuth);
        }
    }

    @FXML
    private void clickButtonRegistration() {
        offTimer();
        login.requestFocus();
        uiClient.getAuthenticateStage().close();
        uiClient.getRegistrationStage().show();
    }

    @Override
    public void onSuccess() {
        Platform.runLater(() -> {
            offTimer();
            uiClient.getAuthenticateStage().close();
            uiClient.getStartStage().setTitle( uiClient.getChatClient().getNick());
            uiClient.getStartStage().show();
            isAuth = true;
        });
    }

    @Override
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
     * Показывает сообщение об ошибке, если пользователь с введенными учетными данными уже зарегистрирован в чате.
     * Очищает поля для ввода логина и пароля и устанавливает фокус на поле для ввода логина.
     */
    @Override
    public void onBusy() {
        Platform.runLater(() -> {
            login.clear();
            password.clear();
            login.requestFocus();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Логин уже занят");
            alert.setHeaderText(null);
            alert.setContentText("Пользователь с такими учётными данными уже есть в чате");
            alert.showAndWait();
        });
    }

    /**
     * Устанавливает фокус на поле для ввода пароля.
     */
    @FXML
    private void nextField() {
        password.requestFocus();
    }

    /**
     * Обрабатывает нажатие на кнопку "Отправить" на экране аутентификации.
     */
    @FXML
    private void nextField1() {
        sendButtonEnter();
    }

    /**
     * Метод для закрытия всех окон
     */
    @Override
    public void closeAllWindows() {
        Platform.runLater(() -> {
            uiClient.getControllerClient().exit();
            uiClient.getAuthenticateStage().close();
            uiClient.getStartStage().close();
            ThreadManagerClient.getInstance().shutdownMyExecutorService();
            ThreadManagerClient.getInstance().shutdownMyScheduledExecutorService();
        });
    }

    @Override
    public void restartTimer() {
        ThreadManagerClient.getInstance().getExecutorService().execute(this::timeLimit);
    }

    @Override
    public void offTimer() {
        scheduledFuture.cancel(false);
    }

    /**
     * Метод для запуска таймера, который закроет окна, если пользователь не авторизовался в течение 120 секунд
     */
    private void timeLimit() {
        try {
            scheduledFuture = ThreadManagerClient.getInstance().getScheduledExecutorService().schedule(() -> {
                if (!isAuth) {
                    timeOffWindow();
                    closeAllWindows();
                }
            }, 120, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void timeOffWindow() {
        Platform.runLater(()->{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Time limit");
            alert.setHeaderText(null);
            alert.setContentText("Время ожидания вышло");
            alert.showAndWait();
        });
    }

}

