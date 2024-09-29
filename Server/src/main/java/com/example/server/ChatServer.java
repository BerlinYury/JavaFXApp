package com.example.server;

import com.example.api.MessageBox;
import com.example.api.MessageType;
import com.example.api.RequestMessage;
import com.example.api.ResponseType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
public class ChatServer implements IChatServer {
    @Inject
    private Instance<ClientHandler> clientHandlerInstance;
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    @Getter
    private ServerSocket serverSocket;

    @Override
    public void run(String[] args) {
        log.info("Server on");
        try {
            serverSocket = new ServerSocket(8129);
            isRunningServerUI(args);
            while (true) {
                Socket textSocket = serverSocket.accept();
                Socket objectSocket = serverSocket.accept();
                ClientHandler clientHandler = clientHandlerInstance.get();
                clientHandler.openConnection(textSocket, objectSocket);
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
    public void sendToAll(RequestMessage requestMessage, String fromNick) {
        String message = String.format("%s %s %s", ResponseType.RESPONSE.getValue(), fromNick,
                requestMessage.getMessage());

        for (ClientHandler client : clients.values()) {
            if (client.getNick().equals(fromNick)) {
                continue;
            }
            client.sendMessage(message);
        }
    }

    @Override
    public void sendToAll(ResponseType responseType, String fromNick) {
        String message = String.format("%s %s", responseType.getValue(), fromNick);

        for (ClientHandler client : clients.values()) {
            if (client.getNick().equals(fromNick)) {
                continue;
            }
            client.sendMessage(message);
        }
    }

    @Override
    public void sendToOneCustomer(RequestMessage requestMessage, String fromNick) {
        String toNick = requestMessage.getNick();
        String message = String.format("%s %s %s", ResponseType.RESPONSE.getValue(), fromNick,
                requestMessage.getMessage());

        if (clients.containsKey(toNick)) {
            clients.get(toNick).sendMessage(message);
        } else {
            DatabaseHandling.addToDBOfflineMessage(new MessageBox.Builder()
                    .type(MessageType.OUTGOING_MESSAGE_FOR_ONE_CUSTOMER)
                    .dateTime(LocalDateTime.now())
                    .message(requestMessage.getMessage())
                    .build(), toNick);
        }
    }

    @Override
    public synchronized void subscribe(String nick, ClientHandler client) {
        clients.put(nick, client);
        sendClientsList();
        sendToAll(ResponseType.USER_ON, nick);
    }

    @Override
    public synchronized void unsubscribe(String nick) {
        clients.remove(nick);
        sendClientsList();
        sendToAll(ResponseType.USER_OFF, nick);
    }

    private void sendClientsList() {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(String.format("%s %s", ResponseType.AUTH_CHANGES.getValue(), clients.keySet()));
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

}
