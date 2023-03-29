package com.example.javafxapp;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ControllerClient {
    @FXML
    private TextArea areaText;
    @FXML
    private TextField fieldText;
    private ChatClient client;

    public ControllerClient() {
        client = new ChatClient(this);
    }

    @FXML
    protected void sendButtonClick() {
        final String msg = fieldText.getText().trim();
        if (msg.isEmpty()) {
            fieldText.requestFocus();
        } else {
            areaText.appendText(msg + "\n");
            client.sendMessage(msg);
            fieldText.clear();
            fieldText.requestFocus();
        }
    }

    public void exit() {
        client.sendMessage(ChatServer.END);
    }

    public void addMessage(String message) {
        areaText.appendText(message + "\n");
    }
}