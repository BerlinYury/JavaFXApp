package com.example.server;


import javafx.fxml.FXML;

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
