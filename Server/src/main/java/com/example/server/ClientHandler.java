package com.example.server;

import com.example.api.MessageBox;
import com.example.api.RequestMessage;
import com.example.api.RequestType;
import com.example.api.ResponseType;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


import static java.util.Objects.*;

@Slf4j
public class ClientHandler implements IClientHandler {
    private Socket textSocket;
    private Socket objectSocket;

    private final ChatServer server;
    private DataInputStream in;
    private DataOutputStream out;
    private ObjectInputStream inObj;
    private ObjectOutputStream outObj;
    private static final Object mon = new Object();
    private String nick;


    public ClientHandler(ChatServer server) {
        this.server = server;
    }

    @Override
    public void openConnection(Socket textSocket, Socket objectSocket) {
        ThreadManagerServer.getInstance().getExecutorService().execute(() -> {
            try {
                this.textSocket = textSocket;
                this.objectSocket = objectSocket;
                in = new DataInputStream(textSocket.getInputStream());
                out = new DataOutputStream(textSocket.getOutputStream());
                outObj = new ObjectOutputStream(objectSocket.getOutputStream());
                inObj = new ObjectInputStream(objectSocket.getInputStream());

                if (isClientAuth()) {
                    readMessages();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    closeConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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
                RequestMessage message = RequestMessage.createMessage(in.readUTF());
                if (isNull(message)) {
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
                        String nick = DatabaseHandling.getNickByLoginAndPassword(message.getLogin(),
                                message.getPassword());
                        if (isNull(nick)) {
                            sendMessage(ResponseType.AUTH_FAILED.getValue());
                            continue;
                        }
                        if (server.isNickBusy(nick)) {
                            sendMessage(ResponseType.AUTH_NICK_BUSY.getValue());
                            continue;
                        }
                        this.nick = nick;
                        sendMessage(String.format("%s %s", ResponseType.AUTH_OK.getValue(), nick));
                        server.subscribe(nick, this);

                        String text = SerializeMessages.readMessagesFromFile(nick);

                        ArrayList<MessageBox> oldMessages = DatabaseHandling.deserializeMessagesFromDB(nick);
                        log.info(String.format("Пользователь %s подключился", this.nick));
                        transmissionTheHistoryOfCorrespondence(oldMessages, text);
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private void readMessages() {
        try {
            while (true) {
                RequestMessage message = RequestMessage.createMessage(in.readUTF());
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
                    case RETENTION -> {
                        SerializeMessages.writeMessagesToFile(message, nick);
                        //TODO добавить подсчёт объектов через RETENTION
                        gettingTheHistoryOfCorrespondence(message);

                    }
                    case START_TRANSFER_OBJECTS, FINISH_TRANSFER_OBJECTS -> {
                        synchronized (mon) {
                            mon.notify();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void transmissionTheHistoryOfCorrespondence(ArrayList<MessageBox> oldMessages, String text) {
        ThreadManagerServer.getInstance().getExecutorServiceSingle().execute(()-> {
            sendMessage(String.format("%s %d %s", ResponseType.RECOVERY.getValue(), oldMessages.size(),
                    text));

            synchronized (mon) {
                try {
                    mon.wait();
                    for (MessageBox messageBox : oldMessages) {
                        outObj.writeObject(messageBox);
                    }
                    mon.wait();
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void gettingTheHistoryOfCorrespondence(RequestMessage message) {
        List<MessageBox> oldMessageSession = new ArrayList<>();
        ThreadManagerServer.getInstance().getExecutorService().execute(() -> {
            try {
                sendMessage(ResponseType.START_TRANSFER_OBJECTS.getValue());
                for (int i = 0; i < message.getCounterObj(); i++) {
                    Object messageBox = inObj.readObject();
                    if (messageBox instanceof MessageBox) {
                        oldMessageSession.add((MessageBox) messageBox);
                    } else {
                        log.error("Объект не соответствует типу MessageBox");
                        throw new IllegalArgumentException("Объект не соответствует типу " +
                                "MessageBox");
                    }
                }
                DatabaseHandling.serializeMessagesToDB(oldMessageSession, nick);
                sendMessage(ResponseType.FINISH_TRANSFER_OBJECTS.getValue());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    private void closeConnection() throws IOException {
        if (nonNull(nick)) {
            server.unsubscribe(nick);
        }
        if (nonNull(in)) {
            in.close();
        }
        if (nonNull(out)) {
            out.close();
        }
        if (nonNull(inObj)) {
            inObj.close();
        }
        if (nonNull(outObj)) {
            outObj.close();
        }
        if (nonNull(objectSocket)) {
            objectSocket.close();
        }
        if (nonNull(textSocket)) {
            textSocket.close();
        }
        log.info(String.format("Пользователь %s отключился", this.nick));
    }

}
