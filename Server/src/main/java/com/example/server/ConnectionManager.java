package com.example.server;

import com.example.api.Group;
import com.example.api.MessageBox;
import com.example.api.Person;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@NoArgsConstructor
@Slf4j
public class ConnectionManager {
    @Getter
    private final ConcurrentHashMap<Person, Session> personSessionMap = new ConcurrentHashMap<>();
    private  DatabaseHandling databaseHandling;


    @Inject
    public ConnectionManager(DatabaseHandling databaseHandling) {
        this.databaseHandling = databaseHandling;
    }

    public synchronized void subscribe(Person person, Session session) {
        personSessionMap.put(person, session);
        transmissionTheHistoryOfCorrespondence(person, session);
        sendGroupWhereIAmAMemberMap(person, session);
        sendClientsListForAll();
    }

    private void sendGroupWhereIAmAMemberMap(Person person,  Session session) {
        List<Group> groupWhereIAmAMember = databaseHandling.getGroupListWhereIAmAMember(person);
        sendMessage(session,new MessageBox.Builder().buildCommandChangeGroupIMemberGroup(groupWhereIAmAMember).toJson());
    }

    private void transmissionTheHistoryOfCorrespondence(Person person, Session session) {
        List<MessageBox> messageBoxList = databaseHandling.deserializeMessagesFromDB(person);
        for (MessageBox messageBox : messageBoxList) {
            sendMessage(session,messageBox.toJson());
        }
    }

    public synchronized void unsubscribe(Person endPerson) {
        if (Objects.nonNull(endPerson)) {
            personSessionMap.remove(endPerson);
            sendClientsListForAll();
        }
    }

    private void sendClientsListForAll() {
        List<Person> activePersonList = new ArrayList<>(personSessionMap.keySet());
        List<Session> activeConnectionList = new ArrayList<>(personSessionMap.values());
        for (Session session : activeConnectionList) {
           sendMessage(session,new MessageBox.Builder().buildCommandChangeStatusPerson(activePersonList).toJson());
        }
    }

    private void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
            // Логируем ошибку
           log.error("Ошибка отправки сообщения: " + e.getMessage());
        }
    }
}
