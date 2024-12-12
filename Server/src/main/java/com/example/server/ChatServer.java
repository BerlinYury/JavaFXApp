package com.example.server;

import com.example.api.Group;
import com.example.api.MessageBox;
import com.example.api.Person;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ChatServer extends WebSocketServer {

    private final DatabaseHandling databaseHandling;
    private final ConcurrentHashMap<Person, WebSocket> personWebSocketMap = new ConcurrentHashMap<>();


    public ChatServer(DatabaseHandling databaseHandling) {
        super(new InetSocketAddress(8129));
        this.databaseHandling=databaseHandling;
    }

    public void run(String[] args) {
        try {
            startServer();
            startUI(args);
        } catch (Exception e) {
            System.out.println(e.getClass().getName());
        }
    }

    private void startServer() {
        this.start();
    }
    private void startUI(String[] args) {
        ThreadManagerServer.getInstance().getExecutorService().execute(() -> {
            UIServer.startFXWindow(args);
        });
    }


    public void sendToGroup(MessageBox messageBox) {
        Group group = messageBox.getGroup();
        List<Person> personInGroupList = group.getPersonInGroupList();
        for (Person personInGroup : personInGroupList) {
            if (personInGroup.getId().equals(messageBox.getOwner().getId())) {
                continue;
            }
            Person newOwner = personInGroup;
            MessageBox reverseMessageBox = new MessageBox.Builder()
                    .buildMessageIncomingGroup(
                            UUID.randomUUID().toString(),
                            messageBox.getDateTime(),
                            newOwner,
                            messageBox.getGroup(),
                            messageBox.getSender(),
                            messageBox.getMessage(),
                            false
                    );
            if (personWebSocketMap.containsKey(personInGroup)) {
                personWebSocketMap.get(personInGroup).send(reverseMessageBox.toJson());
            }
        }
    }

    public void sendToPerson(MessageBox messageBox) {
        Person owner = messageBox.getOwner();
        Person person = messageBox.getPerson();

        Person newOwner = person;
        Person newPerson = owner;

        MessageBox reverseMessageBox = new MessageBox.Builder()
                .buildMessageIncomingPerson(
                        UUID.randomUUID().toString(),
                        messageBox.getDateTime(),
                        newOwner,
                        newPerson,
                        messageBox.getMessage(),
                        false
                );
        if (personWebSocketMap.containsKey(person)) {
            personWebSocketMap.get(person).send(reverseMessageBox.toJson());
        }
    }


    public synchronized void subscribe(Person person, WebSocket conn) {
        personWebSocketMap.put(person, conn);
        transmissionTheHistoryOfCorrespondence(person, conn);
        sendGroupWhereIAmAMemberMap(person, conn);
        sendClientsListForAll();
    }

    public void sendGroupWhereIAmAMemberMap(Person person, WebSocket conn) {
        List<Group> groupWhereIAmAMember = databaseHandling.getGroupListWhereIAmAMember(person);
        conn.send(new MessageBox.Builder().buildCommandChangeGroupIMemberGroup(groupWhereIAmAMember).toJson());
    }

    public void transmissionTheHistoryOfCorrespondence(Person person, WebSocket conn) {
        List<MessageBox> messageBoxList = databaseHandling.deserializeMessagesFromDB(person);
        for (MessageBox messageBox : messageBoxList) {
            conn.send(messageBox.toJson());
        }
    }

    public synchronized void unsubscribe(Person endPerson, WebSocket conn) {
        personWebSocketMap.remove(endPerson);
        sendClientsListForAll();
        conn.close();
    }

    private void sendClientsListForAll() {
        List<Person> activePersonList = new ArrayList<>(personWebSocketMap.keySet());
        List<WebSocket> activeConnectionList = new ArrayList<>(personWebSocketMap.values());
        for (WebSocket conn : activeConnectionList) {
            conn.send(new MessageBox.Builder().buildCommandChangeStatusPerson(activePersonList).toJson());
        }
    }


    public void stopServer() {
        try {
            // Закрываем все активные соединения
            for (WebSocket connection : this.getConnections()) {
                connection.close(1000, "Сервер завершает работу");
            }
            // Останавливаем сервер
            this.stop();
            databaseHandling.getConnection().close();
            ThreadManagerServer.getInstance().shutdownMyExecutorService();
            ServerRunner.getContainer().shutdown();
            System.out.println("Сервер остановлен.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        log.info("Новое соединение: {}", webSocket.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        log.info("Соединение закрыто: {}, причина: {}, удаленный клиент: {}",
                conn.getRemoteSocketAddress(), reason, remote);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            MessageBox messageBox = MessageBox.fromJson(message);

            switch (messageBox.getMessageTypeFirstLevel()) {
                case COMMAND -> workWithCommand(messageBox, conn);
                case MESSAGE -> workWithMessage(messageBox);
                default ->
                        throw new IllegalStateException("Unexpected value: " + messageBox.getMessageTypeFirstLevel());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void workWithMessage(MessageBox messageBox) {
        databaseHandling.addMessageToDB(messageBox);
        switch (messageBox.getMessageTypeFourLevel()) {
            case PERSON -> sendToPerson(messageBox);
            case GROUP -> sendToGroup(messageBox);
            default -> throw new IllegalStateException("Unexpected value: " + messageBox.getMessageTypeFourLevel());
        }
    }

    private void workWithCommand(MessageBox messageBox, WebSocket conn) {
        switch (messageBox.getMessageTypeSecondLevel()) {
            case REQUEST -> workWithRequest(messageBox, conn);
            case END -> {
                Person endPerson = messageBox.getPerson();
                unsubscribe(endPerson, conn);
            }
            default -> throw new IllegalStateException("Unexpected value: " + messageBox.getMessageTypeSecondLevel());
        }
    }

    private void workWithRequest(MessageBox messageBox, WebSocket conn) {
        switch (messageBox.getMessageTypeThirdLevel()) {
            case REG -> workWithReg(messageBox, conn);
            case AUTH -> workWithAuthPerson(messageBox, conn);
            case LIST_ALL_PERSON -> workWithMapAll(messageBox, conn);
            default -> throw new IllegalStateException("Unexpected value: " + messageBox.getMessageTypeThirdLevel());
        }
    }

    private void workWithMapAll(MessageBox messageBox, WebSocket conn) {
        List<Person> allPersonList = databaseHandling.getAllPersonList();
        switch (messageBox.getMessageTypeFourLevel()) {
            case PERSON ->
                    conn.send(new MessageBox.Builder().buildCommandChangeMapAllPersonForPerson(allPersonList).toJson());
            case GROUP ->
                    conn.send(new MessageBox.Builder().buildCommandChangeMapAllPersonForGroup(allPersonList).toJson());
            default -> throw new IllegalStateException("Unexpected value: " + messageBox.getMessageTypeFourLevel());
        }

    }

    private void workWithReg(MessageBox messageBox, WebSocket conn) {
        switch (messageBox.getMessageTypeFourLevel()) {
            case PERSON -> workWithRegPerson(messageBox, conn);
            case GROUP -> workWithRegGroup(messageBox, conn);
            default -> throw new IllegalStateException("Unexpected value: " + messageBox.getMessageTypeFourLevel());
        }
    }

    private void workWithRegPerson(MessageBox messageBox, WebSocket conn) {
        DBResponse dbResponse = databaseHandling.isPersonExistsInDatabase(
                messageBox.getPerson(), messageBox.getEmail()
        );
        if (dbResponse.isFlag()) {
            conn.send(new MessageBox.Builder().buildCommandFailedRegPerson(dbResponse.getErrorOnFieldList()).toJson());
            return;
        }
        databaseHandling.registrationPerson(messageBox.getPerson(), messageBox.getEmail(), messageBox.getPassword());
        conn.send(new MessageBox.Builder().buildCommandAcceptRegPerson().toJson());
    }

    private void workWithRegGroup(MessageBox messageBox, WebSocket conn) {
        if (databaseHandling.isGroupExistsInDatabase(messageBox.getGroup())) {
            conn.send(new MessageBox.Builder().buildCommandFailedRegGroup().toJson());
            return;
        }
        databaseHandling.registrationGroup(messageBox.getGroup());
        conn.send(new MessageBox.Builder().buildCommandAcceptRegGroup(messageBox.getGroup()).toJson());
    }

    private void workWithAuthPerson(MessageBox messageBox, WebSocket conn) {
        Person person = databaseHandling.authenticatePerson(
                messageBox.getEmail(), messageBox.getPassword()
        );
        if (Objects.isNull(person)) {
            conn.send(new MessageBox.Builder().buildCommandFailedAuthPerson().toJson());
            return;
        }
        conn.send(new MessageBox.Builder().buildCommandAcceptAuthPerson(person).toJson());
        subscribe(person, conn);
    }


    @Override
    public void onError(WebSocket webSocket, Exception e) {
        if (webSocket != null) {
            log.error("Ошибка на соединении с клиентом: {}", webSocket.getRemoteSocketAddress(), e);
        } else {
            log.error("Общая ошибка сервера", e);
        }
    }

    @Override
    public void onStart() {
        log.info("WebSocket сервер успешно запущен на порту {}", getPort());
    }
}
