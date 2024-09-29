package com.example.server;

import java.net.Socket;

public interface IClientHandler {
    String getNick();
    void openConnection(Socket socket, Socket objectSocket);
    void sendMessage(String message);
}
