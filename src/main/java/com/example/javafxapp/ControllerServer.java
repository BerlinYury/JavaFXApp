package com.example.javafxapp;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ControllerServer {
    @FXML
    private TextArea areaText;
    @FXML
    private TextField fieldText;
    private ChatServer echoServer;

    public ControllerServer() {
        echoServer = new ChatServer(this);
    }

    @FXML
    protected void sendButtonClick() {
        final String msg = fieldText.getText().trim();
        if (msg.isEmpty()) {
            fieldText.requestFocus();
        } else {
            areaText.appendText(msg + "\n");
            echoServer.sendMessage(msg);
            fieldText.clear();
            fieldText.requestFocus();
        }
    }

    public void exit() {
        echoServer.sendMessage(ChatServer.END);
    }

    public void addMessage(String message) {
        areaText.appendText(message + "\n");
    }
}