package com.example.javafxapp.Server;

public class ServerRunner {
    /**Метод для запуска сервера
     *
     * @param args
     */
    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer(8189);
        chatServer.run();
    }
}
