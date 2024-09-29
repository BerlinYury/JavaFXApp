package com.example.server;

import com.example.api.RequestMessage;
import com.example.api.ResponseType;

public interface IChatServer {
    void run(String[] args);
    boolean isNickBusy(String nick);
    void subscribe(String nick, ClientHandler client);
    void unsubscribe(String nick);
    void sendToAll(RequestMessage requestMessage, String fromNick);
    void sendToAll(ResponseType responseType, String fromNick);
    void sendToOneCustomer(RequestMessage requestMessage, String fromNick);
}
