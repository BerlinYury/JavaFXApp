package com.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private final int port; // Порт, на котором запускается сервер
    private final AuthService authService;// Сервис авторизации

    // Хранилище клиентов чата, где ключ - ник, а значение - объект ClientHandler
    private final ConcurrentHashMap<String, ClientHandler> clients;

    /** Конструктор класса, инициализирует сервис авторизации, хранилище клиентов и порт
     */
    public ChatServer(int port) {
        this.authService = new SimpleAuthService();
        this.clients = new ConcurrentHashMap<>();
        this.port = port;
    }

    /** Метод для запуска сервера
     */
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                final Socket socket = serverSocket.accept();
                new ClientHandler(socket, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Метод для проверки, занят ли данный ник
     */
    public boolean isNickBusy(String nick) {
        return clients.containsKey(nick);
    }

    /** Метод для добавления клиента в хранилище
     */
    public synchronized void subscribe(ClientHandler client) {
        clients.put(client.getNick(), client);
        sendList();
    }

    /** Метод для удаления клиента из хранилища
     */
    public synchronized void unsubscribe(ClientHandler client) {
        clients.remove(client.getNick());
        sendList();
    }

    /** Метод для отправки сообщения конкретному клиенту, или всем клиентам чата
     */
    public void selectiveSendMessage(String msg, ClientHandler client) {
        // Если сообщение предназначено всем клиентам
        if (msg.startsWith(Constants.SEND_TO_ALL.getValue())) {
            String[] s = msg.split(" ", 2);
            String msgWithoutPrefix = s[1];
            String message = String.format("from %s: %s", client.getNick(), msgWithoutPrefix);
            // Отправляем сообщение всем клиентам, кроме отправителя
            for (ClientHandler cl : clients.values()) {
                if (!cl.getNick().equals(client.getNick()))
                    cl.sendMessage(message);
            }
            // Если сообщение предназначено конкретному клиенту
        } else if (msg.startsWith(Constants.SEND_TO_ONE.getValue())) {
            String[] s = msg.split(" ", 3);
            String nick = s[1];
            String msgWithoutPrefix = s[2];
            String message = String.format("from %s: %s", client.getNick(), msgWithoutPrefix);
            // Отправляем сообщение конкретному клиенту, если он есть в хранилище
            if (clients.containsKey(nick)) {
                clients.get(nick).sendMessage(message);
            } else {
                try {
                    throw new Exception("В чате нет пользователя с ником: " + nick);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** Метод для отправки списка клиентов всем клиентам чата, для дальнейшего размещения на кнопках
     */
    public void sendList() {
        for (ClientHandler cl : clients.values()) {
            cl.sendMessage(String.format("%s %s", Constants.AUTH_CHANGES.getValue(), clients.keySet()));
        }
    }

    public AuthService getAuthService() {
        return authService;
    }
}
