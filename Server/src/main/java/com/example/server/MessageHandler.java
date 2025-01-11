package com.example.server;

import com.example.api.Group;
import com.example.api.MessageBox;
import com.example.api.Person;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import jakarta.websocket.Session;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
@Slf4j
@NoArgsConstructor
public class MessageHandler {
    private  ConnectionManager connectionManager;
    private  DatabaseHandling databaseHandling;

    @Inject
    public MessageHandler(ConnectionManager connectionManager, DatabaseHandling databaseHandling) {
        this.connectionManager = connectionManager;
        this.databaseHandling = databaseHandling;
    }

    public void handleMessage(MessageBox messageBox) {
        databaseHandling.addMessageToDB(messageBox);
        switch (messageBox.getMessageTypeFourLevel()) {
            case PERSON -> sendToPerson(messageBox);
            case GROUP -> sendToGroup(messageBox);
            default -> throw new IllegalStateException("Unexpected value: " + messageBox.getMessageTypeFourLevel());
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
        Session session = connectionManager.getPersonSessionMap().get(person);
        if (session != null) {
            sendMessage(session, reverseMessageBox.toJson());
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
                            messageBox.getMessage(),
                            false
                    );
            Session session = connectionManager.getPersonSessionMap().get(personInGroup);
            if (session != null) {
                sendMessage(session, reverseMessageBox.toJson());
            }
        }
    }

    private void sendMessage(Session session, String message) {
        try {;
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("Failed to send message to session {}: {}", session.getId(), e.getMessage(), e);
        }
    }
}
