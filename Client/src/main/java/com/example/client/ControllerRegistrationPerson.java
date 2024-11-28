package com.example.client;

import com.example.api.MessageBox;
import com.example.api.MessageTypeThirdLevel;
import com.example.api.Person;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
public class ControllerRegistrationPerson extends Controller {
    @FXML
    private TextField emailField;
    @FXML
    private TextField nameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField passwordConfirmationField;

    @FXML
    private void clickButtonEnter() {
        String email = emailField.getText().trim();
        String name = nameField.getText().trim();
        String password = Integer.toString(passwordField.getText().trim().hashCode());
        String passwordConfirmation = Integer.toString(passwordConfirmationField.getText().trim().hashCode());

        if (email.isEmpty() || name.isEmpty() || password.isEmpty() || passwordConfirmation.isEmpty()) {
            String title = "Пустые поля";
            String text = "Пожалуйста, заполните все поля";
            showInformationMessage(title, text);
            clearFields();
            return;
        }
        if (password.length() > 12 || password.length() < 6) {
            String title = "Длинна пароля не соответствует диапазону 6-12 символов";
            String text = "Пожалуйста, проверьте правильность введения пароля";
            showInformationMessage(title, text);
            clearFields();
            return;
        }
        if (!password.equals(passwordConfirmation)) {
            String title = "Пароли не совпадают";
            String text = "Пожалуйста, проверьте правильность введения пароля";
            showInformationMessage(title, text);
            clearFields();
            return;
        }
        chatClient.sendMessage(new MessageBox.Builder().buildCommandRequestRegPerson(
                new Person(UUID.randomUUID().toString(), name),
                email, password
        ));
    }

    public void onAcceptRegistrationPerson() {
        Platform.runLater(() -> {
            String title = "Success";
            String text = "Успешная регистрация!";
            showInformationMessage(title, text);
            clearFields();
            registrationStage.close();
            controllerAuthenticate.showAuthenticateStage();
        });
    }

    public void onFailedRegistrationPerson(List<MessageTypeThirdLevel> errorOnFieldList) {
        Platform.runLater(() -> {
            String title = "Ошибка регистрации";
            String text = String.format("Этот пользователь уже существует измените:\n %s", errorOnFieldList.toString());
            showInformationMessage(title, text);
            clearFields();
        });
    }

    @Override
    public void clearFields() {
        emailField.clear();
        nameField.clear();
        passwordField.clear();
        passwordConfirmationField.clear();
        emailField.requestFocus();
    }

    @FXML
    private void nextField() {
        nameField.requestFocus();
    }

    @FXML
    private void nextField0() {
        passwordField.requestFocus();
    }

    @FXML
    private void nextField1() {
        passwordConfirmationField.requestFocus();
    }

    @FXML
    private void nextField2() {
        clickButtonEnter();
    }
}
