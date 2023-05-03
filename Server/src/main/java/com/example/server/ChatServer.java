package com.example.server;

import com.example.api.RequestMessage;
import com.example.api.ResponseType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private final int port; // Порт, на котором запускается сервер
    private final AuthService authService;// Сервис авторизации
    // Хранилище клиентов чата, где ключ - ник, а значение - объект ClientHandler
    private final ConcurrentHashMap<String, ClientHandler> clients;

    /**
     * Конструктор класса, инициализирует сервис авторизации, хранилище клиентов и порт
     */
    public ChatServer(int port) {
        this.authService = new SimpleAuthService();
        this.clients = new ConcurrentHashMap<>();
        this.port = port;
    }

    /**
     * Метод для запуска сервера
     */
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            isRunning(serverSocket);
            while (true) {
                final Socket socket = serverSocket.accept();
                new ClientHandler(socket, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void isRunning(ServerSocket serverSocket) {
        Thread threadServerRunning = new Thread(() -> {
            String commandEnd;
            do {
                Scanner in = new Scanner(System.in);
                commandEnd = in.nextLine();
            } while (!commandEnd.equals("end"));
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        threadServerRunning.setDaemon(true);
        threadServerRunning.setName("threadServerRunning");
        threadServerRunning.start();
    }

    /**
     * Метод для проверки, занят ли данный ник
     */
    public boolean isNickBusy(String nick) {
        return clients.containsKey(nick);
    }

    /**
     * Метод для добавления клиента в хранилище
     */
    public synchronized void subscribe(String nick, ClientHandler client) {
        clients.put(nick, client);
        sendList();
    }

    /**
     * Метод для удаления клиента из хранилища
     */
    public synchronized void unsubscribe(String nick) {
        clients.remove(nick);
        sendList();
    }

    /**
     * Метод для отправки сообщения всем клиентам чата
     */
    public void sendToAll(RequestMessage requestMessage, String fromNick) {
        String message = String.format("%s %s: %s", ResponseType.RESPONSE.getValue(), fromNick, requestMessage.getMessage());
        // Отправляем сообщение всем клиентам, кроме отправителя
        for (ClientHandler client : clients.values()) {
            if (!client.getNick().equals(fromNick))
                client.sendMessage(message);
        }
    }

    /**
     * Метод для отправки сообщения конкретному клиенту чата
     */
    public void sendToOneCustomer(RequestMessage requestMessage, String fromNick) {
        String message = String.format("%s %s: %s", ResponseType.RESPONSE.getValue(), fromNick, requestMessage.getMessage());
        if (clients.containsKey(requestMessage.getNick())) {
            clients.get(requestMessage.getNick()).sendMessage(message);
        } else {
            try {
                throw new Exception(String.format("В чате нет пользователя с ником: %s", requestMessage.getNick()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Метод для отправки списка клиентов всем клиентам чата, для дальнейшего размещения на кнопках
     */
    public void sendList() {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(String.format("%s %s", ResponseType.AUTH_CHANGES.getValue(), clients.keySet()));
        }
    }

    public AuthService getAuthService() {
        return authService;
    }
}
