package com.example.server;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

public class ControllerServer {
    private UIServer uiServer;

    public void setUIServer(UIServer uiServer) {
        this.uiServer = uiServer;
    }

    @FXML
    private void stopButtonClick() {
        uiServer.getStage().close();
    }
}
