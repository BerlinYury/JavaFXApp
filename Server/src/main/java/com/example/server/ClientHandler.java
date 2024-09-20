package com.example.server;

import com.example.api.RequestMessage;
import com.example.api.RequestType;
import com.example.api.ResponseType;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

@Slf4j
public class ClientHandler implements IClientHandler{
    private Socket socket;
    private final ChatServer server;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;


    public ClientHandler(ChatServer server) {
        this.server = server;
    }

    @Override
    public void openConnection(Socket socket) {
        try {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            ThreadManagerServer.getInstance().getExecutorService().execute(() -> {
                try {
                    if (isClientAuth()) {
                        readMessages();
                    }
                } finally {
                    closeConnection();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNick() {
        return nick;
    }

    private boolean isClientAuth() {
        while (true) {
            try {
                String messageStr = in.readUTF();
                System.out.println(messageStr);
                RequestMessage message = RequestMessage.createMessage(messageStr);
                if (message == null) {
                    log.error(String.format("RequestMessage for %s == null", this.nick));
                    continue;
                }
                switch (message.getType()) {
                    case END -> {
                        return false;
                    }
                    case REGISTRATION -> {
                        if (!DatabaseHandling.isClientExistsInDatabase(message.getLogin(), message.getPassword())) {
                            DatabaseHandling.registrationUsers(message.getLogin(), message.getPassword());
                            sendMessage(ResponseType.REG_OK.getValue());
                            log.info(String.format("Пользователь %s успешно зарегистрировался", this.nick));
                        } else {
                            sendMessage(ResponseType.REG_BUSY.getValue());
                            log.info(String.format("Пользователь с такими регистрационными данными ник: %s уже " +
                                    "существует", this.nick));
                        }
                    }
                    case AUTH -> {
                         String nick = SimpleAuthService.getNickFromLoginAndPassword(message.getLogin(),
                                message.getPassword());
                        if (nick == null) {
                            sendMessage(ResponseType.AUTH_FAILED.getValue());
                            continue;
                        }
                        if (server.isNickBusy(nick)) {
                            sendMessage(ResponseType.AUTH_NICK_BUSY.getValue());
                            continue;
                        }
                        this.nick = nick;
                        String oldMessages = SerializeMessages.readMessagesFromFile(nick);
                        sendMessage(String.format("%s %s", ResponseType.RECOVERY.getValue(), oldMessages));
                        sendMessage(String.format("%s %s", ResponseType.AUTH_OK.getValue(), nick));
                        sendMessageAboutChangingCustomerStatus("Пользователь подключился =)");
                        log.info(String.format("Пользователь %s подключился", this.nick));
                        server.subscribe(nick, this);
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private void sendMessageAboutChangingCustomerStatus(String msg) {
        RequestMessage messageChangingCustomerStatus = RequestMessage.createMessage(
                String.format("%s %s", RequestType.SEND_TO_ALL.getValue(), msg)
        );
        if (messageChangingCustomerStatus == null) {
            return;
        }
        server.sendToAll(messageChangingCustomerStatus, nick);
    }

    private void readMessages() {
        try {
            while (true) {
                String messageStr = in.readUTF();
                RequestMessage message = RequestMessage.createMessage(messageStr);
                if (message == null) {
                    log.error(String.format("RequestMessage for %s == null", this.nick));
                    continue;
                }
                switch (message.getType()) {
                    case END -> {
                        return;
                    }
                    case SEND_TO_ALL -> server.sendToAll(message, nick);
                    case SEND_TO_ONE -> server.sendToOneCustomer(message, nick);
                    case RETENTION -> SerializeMessages.writeMessagesToFile(message, nick);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                completionOfWorkWithTheClient();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log.info(String.format("Пользователь %s отключился", this.nick));
    }

    private void completionOfWorkWithTheClient() {
        if (nick == null) {
            return;
        }
        sendMessageAboutChangingCustomerStatus("Пользователь отключился!");
        server.unsubscribe(nick);
    }

}
