package com.example.server;

import com.example.api.RequestMessage;
import com.example.api.ResponseType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.scene.control.Label;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
public class ChatServer implements IChatServer {
    @Inject
    private Instance<ClientHandler> clientHandlerInstance;
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private ExecutorService executorService;
    @Getter
    private ServerSocket serverSocket;

    @Override
    public void run(String[] args) {
        log.info("Server on");
        try {
            serverSocket = new ServerSocket(8129);
            isRunningServerUI(args);
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = clientHandlerInstance.get();
                clientHandler.openConnection(socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ThreadManagerServer.getInstance().shutdownMyExecutorService();
            log.info("Server off");
        }
    }

    @Override
    public boolean isNickBusy(String nick) {
        return clients.containsKey(nick);
    }

    @Override
    public synchronized void subscribe(String nick, ClientHandler client) {
        if (nick == null) {
            log.error("nick == null");
            return;
        }
        clients.put(nick, client);
        sendClientsList();
    }

    @Override
    public synchronized void unsubscribe(String nick) {
        clients.remove(nick);
        sendClientsList();
    }

    @Override
    public void sendToAll(RequestMessage requestMessage, String fromNick) {
        String message = String.format("%s %s: %s", ResponseType.RESPONSE.getValue(), fromNick,
                requestMessage.getMessage());
        // Отправляем сообщение всем клиентам, кроме отправителя
        for (ClientHandler client : clients.values()) {
            if (!client.getNick().equals(fromNick))
                client.sendMessage(message);
        }
    }

    @Override
    public void sendToOneCustomer(RequestMessage requestMessage, String fromNick) {
        String message = String.format("%s %s: %s", ResponseType.RESPONSE.getValue(), fromNick,
                requestMessage.getMessage());
        if (clients.containsKey(requestMessage.getNick())) {
            clients.get(requestMessage.getNick()).sendMessage(message);
        } else {
            try {
                log.error(String.format("В чате нет пользователя с ником: %s", requestMessage.getNick()));
                throw new Exception(String.format("В чате нет пользователя с ником: %s", requestMessage.getNick()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void isRunningServerUI(String[] args) {
        ThreadManagerServer.getInstance().getExecutorService().execute(() -> {
            UIServer.startFXWindow(args);
            serverClose();
        });
    }

    public void serverClose() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendClientsList() {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(String.format("%s %s", ResponseType.AUTH_CHANGES.getValue(), clients.keySet()));
        }
    }

}
