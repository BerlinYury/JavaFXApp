package com.example.server;

public class ServerRunner {
    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer(8129);
        chatServer.run();
    }
}
