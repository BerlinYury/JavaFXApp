package com.example.server;

import java.net.Socket;

public interface IClientHandler {
    String getNick();
    void openConnection(Socket socket);
    void sendMessage(String message);
}
