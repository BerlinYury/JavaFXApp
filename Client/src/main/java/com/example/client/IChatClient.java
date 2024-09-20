package com.example.client;

public interface IChatClient {
    void openConnection();
    void sendMessage(String msg);
    String getNick();
}
