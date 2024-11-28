package com.example.server;


import com.example.api.Group;
import com.example.api.MessageBox;
import com.example.api.MessageTypeFourLevel;
import com.example.api.Person;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssertionMethods {
    private final Connection connection;
    private final DatabaseHandling databaseHandling;

    public AssertionMethods(Connection connection, DatabaseHandling databaseHandling) {
        this.databaseHandling = databaseHandling;
        this.connection = connection;
    }

    public Object[] selectPerson(Person person, String email, String password) {
        List<Object> personArgumentList = new ArrayList<>();
        try {
            databaseHandling.registrationPerson(person, email, password);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement
                    .executeQuery(String.format("select * from person where id='%s'", person.getId()));
            while (resultSet.next()) {
                personArgumentList.add(resultSet.getString(1));
                personArgumentList.add(resultSet.getString(2));
                personArgumentList.add(resultSet.getString(3));
                personArgumentList.add(resultSet.getString(4));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return personArgumentList.toArray();
    }

    public Object[] selectGroup(Group group) {
        List<Object> groupArgumentList = new ArrayList<>();
        List<String> pesonInGroupList = new ArrayList<>();
        try {
            databaseHandling.registrationGroup(group);
            Statement st1 = connection.createStatement();
            Statement st2 = connection.createStatement();
            ResultSet resultSet = st1
                    .executeQuery(String.format("select * from group_of_person where id='%s'",
                            group.getId()));
            ResultSet rsPersonInGroup = st2.executeQuery(
                    String.format("select person_id from person_in_group_of_person where group_of_person_id='%s'",
                            group.getId())
            );
            while (rsPersonInGroup.next()) {
                pesonInGroupList.add(rsPersonInGroup.getString(1));
            }
            Collections.sort(pesonInGroupList);
            while (resultSet.next()) {
                groupArgumentList.add(resultSet.getString(1));
                groupArgumentList.add(resultSet.getString(2));
                groupArgumentList.add(pesonInGroupList);
                groupArgumentList.add(resultSet.getString(3));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return groupArgumentList.toArray();
    }

    public MessageBox selectMessageBox(MessageBox messageBox) {
        MessageBox messageBoxResult = null;
        try {
            databaseHandling.addMessageToDB(messageBox);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    String.format("""
                                            SELECT message.id,
                                         type_table.name,
                                         date,
                                         owner_id,
                                         owner_table.name,
                                         person_id,
                                         person_table.name,
                                         group_of_person_id,
                                         group_of_person_table.name,
                                         sender_id,
                                         sender_table.name,
                                         admin_id,
                                         admin_table.name,
                                         message
                                  FROM chat.message
                                           INNER JOIN type AS type_table ON message.type_id = type_table.id
                                           INNER JOIN person AS owner_table ON message.owner_id = owner_table.id
                                           LEFT JOIN person AS person_table ON message.person_id = person_table.id
                                           LEFT JOIN chat.group_of_person AS group_of_person_table
                                                     ON message.group_of_person_id = group_of_person_table.id
                                           LEFT JOIN person AS sender_table ON message.sender_id = sender_table.id
                                           LEFT JOIN person AS admin_table ON admin_id = admin_table.id
                                  WHERE message.id = '%s';
                                   """,
                            messageBox.getMessageId())
            );
            while (resultSet.next()) {
                String messageId = resultSet.getString(1);
                MessageTypeFourLevel messageType = MessageTypeFourLevel.valueOf(resultSet.getString(2));
                LocalDateTime dateTime = LocalDateTime.parse(resultSet.getString(3), DateTimeFormatter.ofPattern(
                        "yyyy-MM-dd HH:mm:ss"));
                String ownerId = resultSet.getString(4);
                String ownerName = resultSet.getString(5);
                String personId = resultSet.getString(6);
                String personName = resultSet.getString(7);
                String groupId = resultSet.getString(8);
                String groupName = resultSet.getString(9);
                String senderId = resultSet.getString(10);
                String senderName = resultSet.getString(11);
                String adminId = resultSet.getString(12);
                String adminName = resultSet.getString(13);
                String message = resultSet.getString(14);
                List<Person> personInGroupList = databaseHandling.getPersonInGroupList(groupId);
                switch (messageType) {
                    case PERSON -> messageBoxResult = new MessageBox.Builder().buildMessageOutingPerson(
                            messageId,
                            dateTime,
                            new Person(ownerId, ownerName),
                            new Person(personId, personName),
                            message
                    );

                    case GROUP -> messageBoxResult = new MessageBox.Builder().buildMessageOutingGroup(
                            messageId,
                            dateTime,
                            new Person(ownerId, ownerName),
                            new Group(groupId, groupName, personInGroupList, new Person(adminId, adminName)),
                            new Person(senderId, senderName),
                            message
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return messageBoxResult;
    }

    public void cleanAllTables() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("delete from person");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
