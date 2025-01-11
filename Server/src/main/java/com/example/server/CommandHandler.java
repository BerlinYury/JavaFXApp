package com.example.server;

import com.example.api.MessageBox;
import com.example.api.Person;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ApplicationScoped
@NoArgsConstructor
@Slf4j
public class CommandHandler {
    private  ConnectionManager connectionManager;
    private  DatabaseHandling databaseHandling;

    @Inject
    public CommandHandler(ConnectionManager connectionManager, DatabaseHandling databaseHandling) {
        this.connectionManager = connectionManager;
        this.databaseHandling = databaseHandling;
    }

    public void handleCommand(MessageBox messageBox, Session session) {
        switch (messageBox.getMessageTypeSecondLevel()) {
            case REQUEST -> handleRequest(messageBox, session);
            case END -> handleEnd(session);
            default -> throw new IllegalStateException("Unexpected value: " + messageBox.getMessageTypeSecondLevel());
        }
    }
    private void handleEnd(Session session) {
        Person person = findPersonBySession(session);
        if (person != null) {
            connectionManager.unsubscribe(person);
        }
    }

    private Person findPersonBySession(Session session) {
        return connectionManager.getPersonSessionMap().entrySet().stream()
                .filter(entry -> entry.getValue().equals(session))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private void handleRequest(MessageBox messageBox, Session session) {
        switch (messageBox.getMessageTypeThirdLevel()) {
            case REG -> handleRegistration(messageBox, session);
            case AUTH -> handleAuthentication(messageBox, session);
            case LIST_ALL_PERSON -> handleListAllPersons(messageBox, session);
            default -> throw new IllegalStateException("Unexpected value: " + messageBox.getMessageTypeThirdLevel());
        }
    }

    private void handleListAllPersons(MessageBox messageBox, Session session) {
        List<Person> allPersonList = databaseHandling.getAllPersonList();
        switch (messageBox.getMessageTypeFourLevel()) {
            case PERSON -> sendMessage(session, new MessageBox.Builder()
                    .buildCommandChangeMapAllPersonForPerson(allPersonList).toJson());
            case GROUP -> sendMessage(session, new MessageBox.Builder()
                    .buildCommandChangeMapAllPersonForGroup(allPersonList).toJson());
            default -> throw new IllegalStateException("Unexpected value: " + messageBox.getMessageTypeFourLevel());
        }
    }

    private void handleRegistration(MessageBox messageBox, Session session) {
        switch (messageBox.getMessageTypeFourLevel()) {
            case PERSON -> registrationPerson(messageBox, session);
            case GROUP -> registrationGroup(messageBox, session);
            default -> throw new IllegalStateException("Unexpected value: " + messageBox.getMessageTypeFourLevel());
        }
    }

    private void registrationPerson(MessageBox messageBox, Session session) {
        DBResponse dbResponse = databaseHandling.isPersonExistsInDatabase(
                messageBox.getPerson(), messageBox.getEmail()
        );
        if (dbResponse.isFlag()) {
            sendMessage(session, new MessageBox.Builder()
                    .buildCommandFailedRegPerson(dbResponse.getErrorOnFieldList()).toJson());
            return;
        }
        databaseHandling.registrationPerson(messageBox.getPerson(), messageBox.getEmail(), messageBox.getPassword());
        sendMessage(session, new MessageBox.Builder().buildCommandAcceptRegPerson().toJson());
    }

    private void registrationGroup(MessageBox messageBox, Session session) {
        if (databaseHandling.isGroupExistsInDatabase(messageBox.getGroup())) {
            sendMessage(session, new MessageBox.Builder().buildCommandFailedRegGroup().toJson());
            return;
        }
        databaseHandling.registrationGroup(messageBox.getGroup());
        sendMessage(session,new MessageBox.Builder().buildCommandAcceptRegGroup(messageBox.getGroup()).toJson());
    }

    private void handleAuthentication(MessageBox messageBox, Session session) {
        Person person = databaseHandling.authenticatePerson(
                messageBox
                        .getEmail(), messageBox.getPassword()
        );
        if (Objects.isNull(person)) {
            sendMessage(session,new MessageBox.Builder().buildCommandFailedAuthPerson().toJson());
            return;
        }
        sendMessage(session,new MessageBox.Builder().buildCommandAcceptAuthPerson(person).toJson());
        connectionManager.subscribe(person, session);
    }
    private void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("Error sending message to session {}: {}", session.getId(), e.getMessage(), e);
        }
    }

}
