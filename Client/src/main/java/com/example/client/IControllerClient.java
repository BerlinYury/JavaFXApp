package com.example.client;

import com.example.api.MessageBox;
import com.example.api.ResponseMessage;

import java.util.List;

public interface IControllerClient {
    void addIncomingMessage(ResponseMessage message);
    void appendOldMessages(List<MessageBox> oldMessageSession);
    void addButtons(String[] nicks);
    void setLabel();
    void exit();
}
