package com.example.server;

import com.example.api.Group;
import com.example.api.MessageBox;
import com.example.api.Person;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class DataFilling {
    private final DatabaseHandling databaseHandling;

    private ArrayList<MessageBox> messageBoxRegPersonList;
    private ArrayList<Person> personList;
    private ArrayList<MessageBox> messageBoxRegGroupList;
    private ArrayList<Group> groupList;
    private ArrayList<MessageBox> messageBoxList;

    private final int maxCountPerson = 30;
    private final int maxCountGroup = 10;
    private final int minCountPersonInGroup = 2;
    private final int maxCountPersonInGroup = 15;
    private final int maxCountMessage = 1000;

    public DataFilling() {
        this.databaseHandling = new DatabaseHandling("jdbc:mysql://localhost:3306/chat", "root","Cotton68");
    }

    public static void main(String[] args) {
        DataFilling dataFilling = new DataFilling();

        dataFilling.fillPersons();
        dataFilling.fillGroups();
        dataFilling.fillMessageBoxes();

//        dataFilling.cleanAllTables();
    }

    public void fillPersons() {
        messageBoxRegPersonList = new ArrayList<>();
        personList = new ArrayList<>();

        for (int i = 0; i < maxCountPerson; i++) {
            Person person = new Person(UUID.randomUUID().toString(), String.format("person%d", i));
            personList.add(person);
            String email = String.format("email%d", i);
            String password = String.valueOf(String.format("password%d", i).hashCode());
            MessageBox messageBoxRegPerson = new MessageBox.Builder().buildCommandRequestRegPerson(
                    person, email, password);
            messageBoxRegPersonList.add(messageBoxRegPerson);
            databaseHandling.registrationPerson(person, messageBoxRegPerson.getEmail(),
                    messageBoxRegPerson.getPassword());
        }
    }

    public void fillGroups() {
        messageBoxRegGroupList = new ArrayList<>();
        groupList = new ArrayList<>();

        for (int i = 0; i < maxCountGroup; i++) {
            List<Person> personInGroupList = cratePersonInGroupList(messageBoxRegPersonList, minCountPersonInGroup,
                    maxCountPersonInGroup);
            Person admin = personInGroupList.get(0);
            Group group = new Group(UUID.randomUUID().toString(), String.format("group%d", i), personInGroupList,
                    admin);
            groupList.add(group);
            MessageBox messageBoxRegGroup = new MessageBox.Builder().buildCommandRequestRegGroup(group);
            messageBoxRegGroupList.add(messageBoxRegGroup);
            databaseHandling.registrationGroup(group);
        }
    }

    private static List<Person> cratePersonInGroupList(ArrayList<MessageBox> regPersonList, int minCountPersonInGroup,
                                                       int maxCountPersonInGroup) {
        List<Person> personList = new ArrayList<>();
        HashSet<Integer> numsOfPersonInGroup = new HashSet<>();
        int countPersonOfGroup = ThreadLocalRandom.current().nextInt(minCountPersonInGroup,
                Math.min(maxCountPersonInGroup, regPersonList.size()));

        while (numsOfPersonInGroup.size() < countPersonOfGroup) {
            int numberMB = ThreadLocalRandom.current().nextInt(1, regPersonList.size());
            numsOfPersonInGroup.add(numberMB);
        }

        numsOfPersonInGroup.forEach(numberMB -> {
            Person person = regPersonList.get(numberMB).getPerson();
            personList.add(person);
        });

        return personList;
    }

    private void fillMessageBoxes() {
        messageBoxList = new ArrayList<>();

        for (int i = 0; i < maxCountMessage; i++) {
            int numOwner = ThreadLocalRandom.current().nextInt(0, maxCountPerson);
            int numPerson;
            do {
                numPerson = ThreadLocalRandom.current().nextInt(0, maxCountPerson);
            } while (numOwner == numPerson);
            int numGroup = ThreadLocalRandom.current().nextInt(0, maxCountGroup);

            Person owner = messageBoxRegPersonList.get(numOwner).getPerson();
            Person person = messageBoxRegPersonList.get(numPerson).getPerson();
            Group group = messageBoxRegGroupList.get(numGroup).getGroup();
            Person sender = owner;

            MessageBox messageOutingPerson =
                    new MessageBox.Builder().buildMessageOutingPerson(UUID.randomUUID().toString(),
                            LocalDateTime.now().withNano(0), owner, person, String.format("outPerson: %d", i),false);
            messageBoxList.add(messageOutingPerson);
            databaseHandling.addMessageToDB(messageOutingPerson);

            MessageBox messageOutingGroup =
                    new MessageBox.Builder().buildMessageOutingGroup(UUID.randomUUID().toString(),
                            LocalDateTime.now().withNano(0), owner, group, sender, String.format("outGroup: %d", i),false);
            messageBoxList.add(messageOutingGroup);
            databaseHandling.addMessageToDB(messageOutingGroup);
        }
    }

    public void cleanAllTables() {
        try (Connection connection = databaseHandling.createConnection()){
            Statement statement = connection.createStatement();
            statement.executeUpdate("delete from person");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
