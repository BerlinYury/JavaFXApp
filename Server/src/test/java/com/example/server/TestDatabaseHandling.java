package com.example.server;

import com.example.api.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestDatabaseHandling {
    private static DatabaseHandling databaseHandling;
    private static TestDataFilling testDataFilling;
    private static AssertionMethods assertionMethods;

    @BeforeAll
    public static void setUp() {
        databaseHandling = new DatabaseHandling();
        Connection connection = databaseHandling.getConnection();
        assertionMethods = new AssertionMethods(connection, databaseHandling);
        testDataFilling = new TestDataFilling(connection, databaseHandling).fill();
    }

    @ParameterizedTest
    @MethodSource("registrationPersonParameters")
    @Order(1)
    void registrationPerson(Person person, String email, String password) {
        Object[] argsAssert = {person.getId(), email, password, person.getName()};
        Assertions.assertArrayEquals(argsAssert, assertionMethods.selectPerson(person, email, password));
    }

    private static Stream<Arguments> registrationPersonParameters() {
        Person person;
        String email;
        String password;
        List<Arguments> argumentList = new ArrayList<>();

        testDataFilling.getMessageBoxRegPersonList().sort(Comparator.comparing(messageBox -> messageBox.getPerson().getId()));
        for (MessageBox messageBoxRegPerson : testDataFilling.getMessageBoxRegPersonList()) {
            person = messageBoxRegPerson.getPerson();
            email = messageBoxRegPerson.getEmail();
            password = messageBoxRegPerson.getPassword();
            argumentList.add(Arguments.of(person, email, password));
        }
        return argumentList.stream();
    }


    @ParameterizedTest
    @MethodSource("registrationGroupParameters")
    @Order(2)
    void registrationGroup(Group group) {
        List<String> personIdInGroupList = new ArrayList<>();
        for (Person personInGroup : group.getPersonInGroupList()) {
            personIdInGroupList.add(personInGroup.getId());
        }
        Collections.sort(personIdInGroupList);
        Object[] argsAssert = {group.getId(), group.getName(), personIdInGroupList, group.getAdmin().getId()};
        Assertions.assertArrayEquals(argsAssert, assertionMethods.selectGroup(group));
    }

    private static Stream<Arguments> registrationGroupParameters() {
        Group group;
        List<Arguments> argumentList = new ArrayList<>();

        testDataFilling.getMessageBoxRegGroupList().sort(Comparator.comparing(messageBox -> messageBox.getGroup().getId()));
        for (MessageBox messageBoxRegGroup : testDataFilling.getMessageBoxRegGroupList()) {
            group = messageBoxRegGroup.getGroup();
            argumentList.add(Arguments.of(group));
        }
        return argumentList.stream();
    }

    @ParameterizedTest
    @MethodSource("addMessageToDBParameters")
    @Order(3)
    void addMessageToDB(MessageBox messageBoxCheck, MessageBox messageBox) {
        Assertions.assertEquals(messageBoxCheck, messageBox);
    }

    private static Stream<Arguments> addMessageToDBParameters() {
        List<Arguments> argumentList = new ArrayList<>();
        List<MessageBox> messageBoxList = testDataFilling.getMessageBoxList().stream()
                .sorted(Comparator.comparing(MessageBox::getMessageId))
                .toList();

       messageBoxList.forEach(messageBox -> {
           MessageBox mbCheck = assertionMethods.selectMessageBox(messageBox);
           argumentList.add(Arguments.of(mbCheck, messageBox));
       });
        return argumentList.stream();
    }

    @ParameterizedTest
    @MethodSource("isPersonExistsInDatabaseParameters")
    @Order(4)
    void isPersonExistsInDatabase(Person person, String email) {
        Assertions.assertTrue(databaseHandling.isPersonExistsInDatabase(person, email).isFlag());
    }

    private static Stream<Arguments> isPersonExistsInDatabaseParameters() {
        ArrayList<MessageBox> mbPersonList = testDataFilling.getMessageBoxRegPersonList();

        MessageBox messageBox1 = mbPersonList.get(ThreadLocalRandom.current().nextInt(1, mbPersonList.size()));
        Person person1 = messageBox1.getPerson();
        String email1 = messageBox1.getEmail();

        MessageBox messageBox2 = mbPersonList.get(ThreadLocalRandom.current().nextInt(1, mbPersonList.size()));
        Person person2 = messageBox2.getPerson();
        String email2 = "testEmail";

        MessageBox messageBox3 = mbPersonList.get(ThreadLocalRandom.current().nextInt(1, mbPersonList.size()));
        Person person3 = new Person(messageBox3.getPerson().getId(), "testName");
        String email3 = messageBox3.getEmail();

        return Stream.of(Arguments.of(person1, email1), Arguments.of(person2, email2), Arguments.of(person3, email3));
    }

    @ParameterizedTest
    @MethodSource("errorListCheckParameters")
    @Order(5)
    void errorListCheck(Person person, String email, List<MessageTypeThirdLevel> errorListCheck) {
        Assertions.assertArrayEquals(
                errorListCheck.toArray(),
                databaseHandling.isPersonExistsInDatabase(person, email).getErrorOnFieldList().toArray()
        );
    }

    private static Stream<Arguments> errorListCheckParameters() {
        ArrayList<MessageBox> mbPersonList = testDataFilling.getMessageBoxRegPersonList();

        MessageBox messageBox1 = mbPersonList.get(ThreadLocalRandom.current().nextInt(1, mbPersonList.size()));
        Person person1 = messageBox1.getPerson();
        String email1 = messageBox1.getEmail();
        ArrayList<MessageTypeThirdLevel> list1 = new ArrayList<>(List.of(MessageTypeThirdLevel.EMAIL,
                MessageTypeThirdLevel.NAME));

        MessageBox messageBox2 = mbPersonList.get(ThreadLocalRandom.current().nextInt(1, mbPersonList.size()));
        Person person2 = messageBox2.getPerson();
        String email2 = "testEmail";
        ArrayList<MessageTypeThirdLevel> list2 = new ArrayList<>(List.of(MessageTypeThirdLevel.NAME));

        MessageBox messageBox3 = mbPersonList.get(ThreadLocalRandom.current().nextInt(1, mbPersonList.size()));
        Person person3 = new Person(messageBox3.getPerson().getId(), "testName");
        String email3 = messageBox3.getEmail();
        ArrayList<MessageTypeThirdLevel> list3 = new ArrayList<>(List.of(MessageTypeThirdLevel.EMAIL));

        MessageBox messageBox4 = mbPersonList.get(ThreadLocalRandom.current().nextInt(1, mbPersonList.size()));
        Person person4 = new Person(messageBox4.getPerson().getId(), "testName");
        String email4 = "testEmail";
        ArrayList<MessageTypeThirdLevel> list4 = new ArrayList<>();

        return Stream.of(Arguments.of(person1, email1, list1), Arguments.of(person2, email2, list2),
                Arguments.of(person3, email3, list3), Arguments.of(person4, email4, list4));
    }

    @ParameterizedTest
    @MethodSource("isGroupExistsInDatabaseParameters")
    @Order(6)
    void isGroupExistsInDatabase(Group group) {
        Assertions.assertFalse(databaseHandling.isGroupExistsInDatabase(group));
    }

    private static Stream<Arguments> isGroupExistsInDatabaseParameters() {
        ArrayList<Group> groupList = testDataFilling.getGroupList();
        ArrayList<Person> personList = testDataFilling.getPersonList();

        Group oldGroup2 = groupList.get(ThreadLocalRandom.current().nextInt(1, groupList.size()));
        Group group2 = new Group(oldGroup2.getId(), "groupTestName2", oldGroup2.getPersonInGroupList(),
                oldGroup2.getAdmin());

        Group oldGroup3 = groupList.get(ThreadLocalRandom.current().nextInt(1, groupList.size()));
        List<Person> personInGroupList3 =
                new ArrayList<>(List.of(personList.get(ThreadLocalRandom.current().nextInt(1, personList.size())),
                        personList.get(ThreadLocalRandom.current().nextInt(1, personList.size())),
                        personList.get(ThreadLocalRandom.current().nextInt(1, personList.size()))));
        Group group3 = new Group(oldGroup3.getId(), oldGroup3.getName(), personInGroupList3, oldGroup3.getAdmin());

        Group oldGroup4 = groupList.get(ThreadLocalRandom.current().nextInt(1, groupList.size()));
        List<Person> personInGroupList4 =
                new ArrayList<>(List.of(personList.get(ThreadLocalRandom.current().nextInt(1, personList.size())),
                        personList.get(ThreadLocalRandom.current().nextInt(1, personList.size())),
                        personList.get(ThreadLocalRandom.current().nextInt(1, personList.size()))));
        Group group4 = new Group(oldGroup4.getId(), "groupTestName4", personInGroupList4, oldGroup4.getAdmin());

        return Stream.of(Arguments.of(group2), Arguments.of(group3), Arguments.of(group4));
    }

    @Test
    @Order(7)
    void authenticatePerson() {
        ArrayList<MessageBox> mbPersonList = testDataFilling.getMessageBoxRegPersonList();

        MessageBox messageBox1 = mbPersonList.get(ThreadLocalRandom.current().nextInt(1, mbPersonList.size()));
        Person person = messageBox1.getPerson();
        String email = messageBox1.getEmail();
        String password = messageBox1.getPassword();

        Assertions.assertEquals(person, databaseHandling.authenticatePerson(email, password));
    }

    @Test
    @Order(8)
    void getAllPersonList() {
        ArrayList<Person> personList = testDataFilling.getPersonList();
        personList.sort(Comparator.comparing(Unit::getId));
        List<Person> allPersonList = databaseHandling.getAllPersonList();
        allPersonList.sort(Comparator.comparing(Unit::getId));
        Assertions.assertArrayEquals(personList.toArray(), allPersonList.toArray());
    }

    @Test
    @Order(9)
    void getPersonInGroupList() {
        ArrayList<Group> groupList = testDataFilling.getGroupList();
        Group group = groupList.get(ThreadLocalRandom.current().nextInt(1, groupList.size()));
        List<Person> personInGroupList = group.getPersonInGroupList();
        personInGroupList.sort(Comparator.comparing(Unit::getId));
        List<Person> personInGroupListFromDB = databaseHandling.getPersonInGroupList(group.getId());
        personInGroupList.sort(Comparator.comparing(Unit::getId));
        Assertions.assertArrayEquals(personInGroupList.toArray(), personInGroupListFromDB.toArray());
    }

    @Test
    @Order(10)
    void getGroupListWhereIAmAMember() {
        ArrayList<Group> groupWhenIMemberListCheck = new ArrayList<>();
        ArrayList<Person> personList = testDataFilling.getPersonList();
        Person person = personList.get(ThreadLocalRandom.current().nextInt(1, personList.size()));
        for (Group group : testDataFilling.getGroupList()) {
            for (Person personFromGroup : group.getPersonInGroupList()) {
                if (personFromGroup.equals(person)) {
                    groupWhenIMemberListCheck.add(group);
                }
            }
        }
        groupWhenIMemberListCheck.sort(Comparator.comparing(Unit::getId));
        List<Group> groupListWhereIAmAMember = databaseHandling.getGroupListWhereIAmAMember(person);
        groupListWhereIAmAMember.sort(Comparator.comparing(Unit::getId));
        Assertions.assertArrayEquals(groupWhenIMemberListCheck.toArray(), groupListWhereIAmAMember.toArray());
    }


    @ParameterizedTest
    @MethodSource("deserializeMessagesFromDBParameters")
    @Order(11)
    void deserializeMessagesFromDB(MessageBox expected, MessageBox actual) {
        Assertions.assertEquals(expected, actual);
    }

    private static Stream<Arguments> deserializeMessagesFromDBParameters() {
        List<Arguments> argumentsList = new ArrayList<>();
        ArrayList<Person> personList = testDataFilling.getPersonList();
        ArrayList<Group> groupList = testDataFilling.getGroupList();
        Person person = personList.get(ThreadLocalRandom.current().nextInt(1, personList.size()));

        List<Group> groups = groupList.stream().filter(group -> group.getPersonInGroupList().contains(person)).toList();

        List<MessageBox> messageBoxListFromOwner =
                testDataFilling.getMessageBoxList().stream().filter(messageBox -> messageBox.getOwner().equals(person) || Objects.nonNull(messageBox.getPerson()) && messageBox.getPerson().equals(person) || groups.contains(messageBox.getGroup())).toList();

        List<MessageBox> messageBoxList = new ArrayList<>();
        for (MessageBox messageBox : messageBoxListFromOwner) {
            if (messageBox.getOwner().equals(person)) {
                messageBoxList.add(messageBox);
            } else {
                switch (messageBox.getMessageTypeFourLevel()) {
                    case PERSON ->
                            messageBoxList.add(new MessageBox.Builder().buildMessageIncomingPerson(UUID.randomUUID().toString(), messageBox.getDateTime(), messageBox.getPerson(), messageBox.getOwner(), messageBox.getMessage(),false));
                    case GROUP ->
                            messageBoxList.add(new MessageBox.Builder().buildMessageIncomingGroup(UUID.randomUUID().toString(), messageBox.getDateTime(), person, messageBox.getGroup(), messageBox.getSender(), messageBox.getMessage(),false));
                }
            }
        }
        messageBoxList.sort(Comparator.comparing(MessageBox::getMessage));

        List<MessageBox> messageBoxListFromDB =
                databaseHandling.deserializeMessagesFromDB(person).stream().sorted(Comparator.comparing(MessageBox::getMessage)).toList();

        for (int i = 0; i < messageBoxList.size(); i++) {
            MessageBox expected = messageBoxList.get(i);
            MessageBox actual = messageBoxListFromDB.get(i);
            argumentsList.add(Arguments.of(expected, actual));
        }
        return argumentsList.stream();
    }

    @AfterAll
    public static void cleanDB(){
        assertionMethods.cleanAllTables();
    }

}
