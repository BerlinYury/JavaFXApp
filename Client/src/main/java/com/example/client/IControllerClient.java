package com.example.client;

import com.example.api.ResponseMessage;

public interface IControllerClient {
    void addIncomingMessage(ResponseMessage message);
    void appendOldMessages(String oldMessages);
    void addButtons(String[] nicks);
    void viewWindow();
    void setLabel();
    void exit();
}
