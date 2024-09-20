package com.example.server;

import com.example.api.RequestMessage;

public interface IChatServer {
    void run(String[] args);
    boolean isNickBusy(String nick);
    void subscribe(String nick, ClientHandler client);
    void unsubscribe(String nick);
    void sendToAll(RequestMessage requestMessage, String fromNick);
    void sendToOneCustomer(RequestMessage requestMessage, String fromNick);
}
