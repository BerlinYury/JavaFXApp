package com.example.client;

import com.example.api.RequestType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class ControllerRegistration extends Controller implements IControllerRegistration{
    @FXML
    private PasswordField passwordConfirmation;
    @FXML
    private PasswordField password;
    @FXML
    private TextField login;

    @Setter
    private UIClient uiClient;


    @FXML
    private void sendButtonEnter() {
        String loginText = login.getText().trim();
        String passwordText = password.getText().trim();
        String passwordConfirmationText = passwordConfirmation.getText().trim();

        if (loginText.isEmpty() || passwordText.isEmpty() || passwordConfirmationText.isEmpty()) {
            String title = "Пустые поля";
            String text = "Пожалуйста, заполните все поля";
            registrationDataEntryError(title, text);
            return;
        }
        if (passwordText.length() > 12 || passwordText.length() < 6) {
            String title = "Длинна пароля не соответствует диапазону 6-12 символов";
            String text = "Пожалуйста, проверьте правильность введения пароля";
            registrationDataEntryError(title, text);
            return;
        }
        if (!passwordText.equals(passwordConfirmationText)) {
            String title = "Пароли не совпадают";
            String text = "Пожалуйста, проверьте правильность введения пароля";
            registrationDataEntryError(title, text);
            return;
        }
        login.clear();
        password.clear();
        passwordConfirmation.clear();
        var msgReg = String.format("%s %s %s", RequestType.REGISTRATION.getValue(), loginText, passwordText);
        uiClient.getChatClient().sendMessage(msgReg);
    }

    @Override
    public void onSuccess() {
        Platform.runLater(() -> {
            uiClient.getControllerAuthenticate().restartTimer();
            uiClient.getAuthenticateStage().show();
            uiClient.getRegistrationStage().close();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Успешная регистрация!");
            alert.showAndWait();
        });
    }

    @Override
    public void onBusy() {
        Platform.runLater(() -> {
            login.clear();
            password.clear();
            passwordConfirmation.clear();
            login.requestFocus();
            String title = "Пользователь с таким логином уже есть";
            String text = "Пожалуйста, придумайте другой логин";
            registrationDataEntryError(title, text);
        });
    }

    @FXML
    private void nextField() {
        password.requestFocus();
    }

    @FXML
    private void nextField1() {
        passwordConfirmation.requestFocus();
    }

    @FXML
    private void nextField2() {
        sendButtonEnter();
    }

    private void registrationDataEntryError(String title, String text) {
        login.clear();
        password.clear();
        passwordConfirmation.clear();
        login.requestFocus();
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}
