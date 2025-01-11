package com.example.client;

import com.example.api.MessageBox;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ControllerAuthenticate extends Controller {
    @FXML
    private TextField emailField;
    @FXML
    private TextField passwordField;
    private boolean isAuth = false;
    private ScheduledFuture<?> scheduledFuture;

    public ControllerAuthenticate() {
        ThreadManagerClient.getInstance().getExecutorService().execute(this::timeLimit);
    }

    @FXML
    public void clickButtonEnter() {
        String login = emailField.getText().trim();
        String password = Integer.toString(passwordField.getText().trim().hashCode());
        if (login.isEmpty() || password.isEmpty()) {
            String title = ("Пустые поля");
            String text = ("Пожалуйста, заполните поля Логин и Пароль");
            showInformationMessage(title, text);
            clearFields();
        } else {
            chatClient.sendMessage(new MessageBox.Builder().buildCommandRequestAuthPerson(login, password));
        }
    }

    @FXML
    private void clickButtonRegistration() {
        offTimer();
        emailField.requestFocus();
        authenticateStage.close();
        registrationStage.show();
    }

    public void showAuthenticateStage() {
        Platform.runLater(() -> {
        authenticateStage.show();
        });
    }

    public void onAcceptAuthenticatePerson() {
        Platform.runLater(() -> {
            offTimer();
            authenticateStage.close();
            controllerClient.showStartStage();
            isAuth = true;
        });
    }

    public void onFailedAuthenticatePerson() {
        Platform.runLater(() -> {
            String title = "Ошибка авторизации";
            String text = "Вы ввели неверный email или password, попробуйте снова ";
            showInformationMessage(title, text);
            clearFields();
        });
    }

    private void timeLimit() {
        try {
            scheduledFuture = ThreadManagerClient.getInstance().getScheduledExecutorService().schedule(() -> {
                if (!isAuth) {
                    chatClient.stopClient();
                    chatClient.closeAllResources();
                    Platform.runLater(Platform::exit);
                }
            }, 120, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void restartTimer() {
        ThreadManagerClient.getInstance().getExecutorService().execute(this::timeLimit);
    }

    public void offTimer() {
        scheduledFuture.cancel(false);
    }

    @Override
    public void clearFields() {
        emailField.clear();
        passwordField.clear();
        emailField.requestFocus();
    }

    @FXML
    private void nextField() {
        passwordField.requestFocus();
    }


    @FXML
    private void nextField1() {
        clickButtonEnter();
    }
}

