package com.example.server;

import com.example.api.Group;
import com.example.api.MessageBox;
import com.example.api.Person;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
public class ChatServer {
    @Inject
    private Instance<ClientHandler> clientHandlerInstance;
    @Inject
    private DatabaseHandling databaseHandling;
    private final ConcurrentHashMap<Person, ClientHandler> clientHandlerMap = new ConcurrentHashMap<>();
    @Getter
    private ServerSocket serverSocket;


    public void run(String[] args) {
        try {
            serverSocket = new ServerSocket(8129);
            isRunningServerUI(args);
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = clientHandlerInstance.get();
                clientHandler.openConnection(socket);
            }
        } catch (Exception e) {
            System.out.println(e.getClass().getName());
        } finally {
            ThreadManagerServer.getInstance().shutdownMyExecutorService();
        }
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
                            messageBox.getMessage()
                    );
            if (clientHandlerMap.containsKey(personInGroup)) {
                clientHandlerMap.get(personInGroup).sendMessage(reverseMessageBox);
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
                        messageBox.getMessage()
                );
        if (clientHandlerMap.containsKey(person)) {
            clientHandlerMap.get(person).sendMessage(reverseMessageBox);
        }
    }


    public synchronized void subscribe(Person person, ClientHandler clientHandler) {
        clientHandlerMap.put(person, clientHandler);
        List<MessageBox> messageBoxList = databaseHandling.deserializeMessagesFromDB(person);
        clientHandlerMap.get(person).transmissionTheHistoryOfCorrespondence(messageBoxList);
        List<Group> groupWhereIAmAMember = databaseHandling.getGroupListWhereIAmAMember(person);
        clientHandlerMap.get(person).sendGroupWhereIAmAMemberMap(groupWhereIAmAMember);
        sendClientsList();
    }

    public synchronized void unsubscribe(Person person) {
        clientHandlerMap.remove(person);
        sendClientsList();
    }

    private void sendClientsList() {
      List<Person> activePersonList = new ArrayList<>(clientHandlerMap.keySet());
        for (ClientHandler clientHandler : clientHandlerMap.values()) {
            clientHandler.sendMessage(
                    new MessageBox.Builder()
                            .buildCommandChangeStatusPerson(activePersonList));
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
            databaseHandling.getConnection().close();
            serverSocket.close();
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
