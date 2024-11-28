package com.example.server;

import com.example.api.*;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@ApplicationScoped
public class DatabaseHandling {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    @Getter
    private final Connection connection;

    public DatabaseHandling() {
        this.connection = createConnection();
    }

    private static Connection createConnection() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/chat?user=root&password=Cotton68");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void registrationPerson(Person person, String email, String password) {
        lock.writeLock().lock();
        try (Connection connection = createConnection()) {
            connection.setAutoCommit(false);

            String personId = person.getId();
            String personName = person.getName();

            try (PreparedStatement personStmt = connection.prepareStatement(
                    "INSERT INTO person (id, email, password, name) VALUES (?, ?, ?, ?)"
            )) {
                personStmt.setString(1, personId);
                personStmt.setString(2, email);
                personStmt.setString(3, password);
                personStmt.setString(4, personName);
                personStmt.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void registrationGroup(Group group) {
        lock.writeLock().lock();
        try (Connection connection = createConnection()) {
            connection.setAutoCommit(false);

            String groupId = group.getId();
            String groupName = group.getName();
            Person admin = group.getAdmin();
            String adminId = admin.getId();

            try (PreparedStatement groupStmt = connection.prepareStatement(
                    "INSERT INTO group_of_person (id, name, admin_id) VALUES (?, ?, ?)"
            )) {
                groupStmt.setString(1, groupId);
                groupStmt.setString(2, groupName);
                groupStmt.setString(3, adminId);
                groupStmt.executeUpdate();

                addPersonsToGroup(group, connection);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void addPersonsToGroup(Group group, Connection connection) throws SQLException {
        List<Person> personInGroupList = group.getPersonInGroupList();
        String groupId = group.getId();

        PreparedStatement preparedStatement = connection.prepareStatement("""
                                                                          insert into person_in_group_of_person 
                                                                          (person_id, group_of_person_id) values (?,?);
                                                                          """);
        preparedStatement.setString(2, groupId);
        for (Person person : personInGroupList) {
            preparedStatement.setString(1, person.getId());
            preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
    }

    public DBResponse isPersonExistsInDatabase(Person person, String email) {
        lock.writeLock().lock();
        ArrayList<MessageTypeThirdLevel> errorOnFieldList = new ArrayList<>();
        try {
            ResultSet resultSetEmail = connection.createStatement().executeQuery(
                    String.format("SELECT EXISTS (select * from person where email = '%s')", email)
            );
            resultSetEmail.next();
            if (resultSetEmail.getInt(1) > 0) {
                errorOnFieldList.add(MessageTypeThirdLevel.EMAIL);
            }

            ResultSet resultSet = connection.createStatement().executeQuery(
                    String.format("SELECT EXISTS (select * from person where name = '%s')", person.getName())
            );
            resultSet.next();
            if (resultSet.getInt(1) > 0) {
                errorOnFieldList.add(MessageTypeThirdLevel.NAME);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }

        if (errorOnFieldList.isEmpty()) {
            return new DBResponse(false, errorOnFieldList);
        } else {
            return new DBResponse(true, errorOnFieldList);
        }
    }


    public boolean isGroupExistsInDatabase(Group group) {
        lock.writeLock().lock();
        try {
            String newGroupName = group.getName();
            Person newAdmin = group.getAdmin();
            List<Group> groupWhereIAmAMemberList = getGroupListWhereIAmAMember(newAdmin);
            List<Person> newPersonInGroupList = group.getPersonInGroupList();
            newPersonInGroupList.sort(Comparator.comparing(Unit::getId));
            for (var groupWhenIMember : groupWhereIAmAMemberList) {
                List<Person> personInGroupListWhenIMember = groupWhenIMember.getPersonInGroupList();
                personInGroupListWhenIMember.sort(Comparator.comparing(Unit::getId));
                if (groupWhenIMember.getName().equals(newGroupName)
                        && groupWhenIMember.getPersonInGroupList().equals(newPersonInGroupList)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
        return false;
    }

    public List<Group> getGroupListWhereIAmAMember(Person person) {
        List<Group> groupWhereIAmAMember = new ArrayList<>();
        try {
            Statement selectStatement = connection.createStatement();
            String sql = """
                         select group_of_person.id,
                                group_of_person.name,
                                group_of_person.admin_id,
                                person.name
                         from person_in_group_of_person
                                  inner join group_of_person
                                             on person_in_group_of_person.group_of_person_id = group_of_person.id
                         inner join person on admin_id =person.id
                         where person_id = '%s'
                         """;
            ResultSet resultSet = selectStatement.executeQuery(String.format(sql, person.getId()));
            while (resultSet.next()) {
                String groupId = resultSet.getString(1);
                String groupName = resultSet.getString(2);
                List<Person> personInGroupList = getPersonInGroupList(groupId);
                String adminId = resultSet.getString(3);
                String adminName = resultSet.getString(4);
                Person admin = new Person(adminId, adminName);
                Group group = new Group(groupId, groupName, personInGroupList, admin);
                groupWhereIAmAMember.add(group);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return groupWhereIAmAMember;
    }

    public Person authenticatePerson(String email, String password) {
        lock.readLock().lock();
        try {
            Statement selectStatement = connection.createStatement();
            String sql = """
                         select id, name from person where email = '%s' AND password = '%s'
                         """;
            String sqlRequest = String.format(sql, email, password);
            ResultSet rs = selectStatement.executeQuery(sqlRequest);
            if (rs.next()) {
                return new Person(rs.getString(1), rs.getString(2));
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<MessageBox> deserializeMessagesFromDB(Person ownerOfHistory) {
        lock.readLock().lock();
        ConcurrentHashMap<String, Unit> idAndUnitMap = new ConcurrentHashMap<>();
        List<MessageBox> messageBoxList = new ArrayList<>();

        try {
            List<Group> groupWhereIAmAMember = getGroupListWhereIAmAMember(ownerOfHistory);
            StringBuilder groupIdBuilder = new StringBuilder();
            if (groupWhereIAmAMember.isEmpty()) {
                groupIdBuilder.append("''");
            } else {
                groupWhereIAmAMember.forEach(group -> groupIdBuilder.append(String.format("'%s',", group.getId())));
                groupIdBuilder.deleteCharAt(groupIdBuilder.length() - 1);
            }
            Statement selectStatement = connection.createStatement();
            String sql = """
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
                         WHERE message.owner_id = '%s'
                            OR message.person_id = '%s'
                            OR message.group_of_person_id IN (%s)
                         ORDER BY date;
                         """;
            String formatted = String.format(sql, ownerOfHistory.getId(), ownerOfHistory.getId(), groupIdBuilder);

            ResultSet resultSet = selectStatement.executeQuery(formatted);
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

                if (ownerId.equals(ownerOfHistory.getId())) {
                    switch (messageType) {
                        case PERSON -> {
                            Person owner;
                            if (idAndUnitMap.containsKey(ownerId)) {
                                owner = (Person) idAndUnitMap.get(ownerId);
                            } else {
                                owner = new Person(ownerId, ownerName);
                                idAndUnitMap.put(ownerId, owner);
                            }

                            Person person;
                            if (idAndUnitMap.containsKey(personId)) {
                                person = (Person) idAndUnitMap.get(personId);
                            } else {
                                person = new Person(personId, personName);
                                idAndUnitMap.put(personId, person);
                            }

                            messageBoxList.add(new MessageBox.Builder()
                                    .buildMessageOutingPerson(messageId, dateTime, owner, person, message)
                            );
                        }
                        case GROUP -> {
                            Person owner;
                            if (idAndUnitMap.containsKey(ownerId)) {
                                owner = (Person) idAndUnitMap.get(ownerId);
                            } else {
                                owner = new Person(ownerId, ownerName);
                                idAndUnitMap.put(ownerId, owner);
                            }

                            Person admin;
                            if (idAndUnitMap.containsKey(adminId)) {
                                admin = (Person) idAndUnitMap.get(adminId);
                            } else {
                                admin = new Person(adminId, adminName);
                                idAndUnitMap.put(adminId, admin);
                            }

                            Group group;
                            if (idAndUnitMap.containsKey(groupId)) {
                                group = (Group) idAndUnitMap.get(groupId);
                            } else {
                                group = new Group(groupId, groupName,
                                        getPersonInGroupList(groupId),
                                        admin);
                            }

                            Person sender;
                            if (idAndUnitMap.containsKey(senderId)) {
                                sender = (Person) idAndUnitMap.get(senderId);
                            } else {
                                sender = new Person(senderId, senderName);
                                idAndUnitMap.put(senderId, sender);
                            }

                            messageBoxList.add(new MessageBox.Builder()
                                    .buildMessageOutingGroup(
                                            messageId, dateTime, owner,
                                            group, sender, message)
                            );
                        }
                    }
                } else {
                    switch (messageType) {
                        case PERSON -> {
                            Person owner;
                            if (idAndUnitMap.containsKey(ownerId)) {
                                owner = (Person) idAndUnitMap.get(ownerId);
                            } else {
                                owner = new Person(ownerId, ownerName);
                                idAndUnitMap.put(ownerId, owner);
                            }

                            Person person;
                            if (idAndUnitMap.containsKey(personId)) {
                                person = (Person) idAndUnitMap.get(personId);
                            } else {
                                person = new Person(personId, personName);
                                idAndUnitMap.put(personId, person);
                            }


                            Person newOwner = person;
                            Person newPerson = owner;

                            messageBoxList.add(new MessageBox.Builder()
                                    .buildMessageIncomingPerson(
                                            UUID.randomUUID().toString(), dateTime, newOwner,
                                            newPerson, message)
                            );
                        }
                        case GROUP -> {
                            Person owner;
                            if (idAndUnitMap.containsKey(ownerId)) {
                                owner = (Person) idAndUnitMap.get(ownerId);
                            } else {
                                owner = new Person(ownerId, ownerName);
                                idAndUnitMap.put(ownerId, owner);
                            }

                            Person admin;
                            if (idAndUnitMap.containsKey(adminId)) {
                                admin = (Person) idAndUnitMap.get(adminId);
                            } else {
                                admin = new Person(adminId, adminName);
                                idAndUnitMap.put(adminId, admin);
                            }

                            Group group;
                            if (idAndUnitMap.containsKey(groupId)) {
                                group = (Group) idAndUnitMap.get(groupId);
                            } else {
                                group = new Group(groupId, groupName,
                                        getPersonInGroupList(groupId),
                                        admin);
                            }

                            Person sender;
                            if (idAndUnitMap.containsKey(senderId)) {
                                sender = (Person) idAndUnitMap.get(senderId);
                            } else {
                                sender = new Person(senderId, senderName);
                                idAndUnitMap.put(senderId, sender);
                            }

                            Person newOwner= ownerOfHistory;
                            messageBoxList.add(new MessageBox.Builder()
                                    .buildMessageIncomingGroup(
                                            UUID.randomUUID().toString(), dateTime, newOwner,
                                            group, sender, message));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }
        return messageBoxList;
    }

    public List<Person> getPersonInGroupList(String groupId) {
        List<Person> personInGroupList = new ArrayList<>();
        String sql = """
                     select person_id, person.name from person_in_group_of_person
                     inner join person on person_in_group_of_person.person_id = person.id
                     where group_of_person_id = ?;
                     """;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, groupId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                String personId = rs.getString(1);
                String personName = rs.getString(2);
                personInGroupList.add(new Person(personId, personName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return personInGroupList;
    }

    public void addMessageToDB(MessageBox messageBox) {
        lock.writeLock().lock();
        try {
            Statement statement = connection.createStatement();
            String messageId = messageBox.getMessageId();
            int typeId = getTypeId(connection, messageBox.getMessageTypeFourLevel());
            String date = messageBox.getDateTime().toString();
            String ownerId = messageBox.getOwner().getId();
            String message = messageBox.getMessage();
            switch (messageBox.getMessageTypeFourLevel()) {
                case PERSON -> {
                    String personId = messageBox.getPerson().getId();
                    String sql = """
                                 insert into chat.message (id, type_id, date, owner_id, person_id, group_of_person_id, sender_id, message)
                                 values ('%s','%s','%s','%s','%s',%s,%s,'%s');
                                 """;
                    statement.executeUpdate(String.format(sql, messageId, typeId, date, ownerId, personId,
                            null, null, message));
                }
                case GROUP -> {
                    String groupId = messageBox.getGroup().getId();
                    String senderId = messageBox.getSender().getId();
                    String sql = """
                                 insert into chat.message (id, type_id, date, owner_id, person_id, group_of_person_id, sender_id, message)
                                 values ('%s','%s','%s','%s',%s,'%s','%s','%s');
                                 """;
                    statement.executeUpdate(String.format(sql, messageId, typeId, date, ownerId, null,
                            groupId, senderId, message));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private int getTypeId(Connection connection, MessageTypeFourLevel type) throws SQLException {
        Statement selectStatement = connection.createStatement();
        String sql = """
                     select id from type where name = '%s'
                     """;
        ResultSet resultSet = selectStatement.executeQuery(String.format(sql, type));
        if (resultSet.next()) {
            return resultSet.getInt(1);
        } else {
            throw new IllegalArgumentException(String.format("тип сообщения %s не содержится в базе", type));
        }
    }

    public List<Person> getAllPersonList() {
        lock.readLock().lock();
        List<Person> allPersonList = new ArrayList<>();
        try {
            Statement selectStatement = connection.createStatement();
            String sql = """
                         select id, name from person;
                         """;
            ResultSet rs = selectStatement.executeQuery(sql);
            while (rs.next()) {
                String id = rs.getString(1);
                String name = rs.getString(2);
                allPersonList.add(new Person(id, name));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }
        return allPersonList;
    }
}