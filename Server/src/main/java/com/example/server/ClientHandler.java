package com.example.server;

import com.example.api.Group;
import com.example.api.MessageBox;
import com.example.api.MessageTypeThirdLevel;
import com.example.api.Person;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.*;

@Slf4j
public class ClientHandler {
    private Socket socket;
    private final ChatServer server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Person myPerson;
    private final DatabaseHandling databaseHandling;


    public ClientHandler(ChatServer server,DatabaseHandling databaseHandling) {
        this.server = server;
        this.databaseHandling=databaseHandling;
    }

    public void openConnection(Socket socket) {
        ThreadManagerServer.getInstance().getExecutorService().execute(() -> {
            try {
                this.socket = socket;
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                readMessages();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void sendMessage(MessageBox messageBox) {
        try {
            out.writeObject(messageBox);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessages() {
        try {
            while (true) {
                MessageBox messageBox = (MessageBox) in.readObject();
                switch (messageBox.getMessageTypeFirstLevel()) {
                    case COMMAND -> workWithCommand(messageBox);
                    case MESSAGE -> workWithMessage(messageBox);
                    default -> showIllegalStateException(messageBox);
                }
            }
        } catch (SocketException e) {
            System.out.println(e.getMessage());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void workWithMessage(MessageBox messageBox) {
        switch (messageBox.getMessageTypeSecondLevel()) {
            case OUTGOING -> {
                databaseHandling.addMessageToDB(messageBox);
                switch (messageBox.getMessageTypeFourLevel()) {
                    case PERSON -> server.sendToPerson(messageBox);
                    case GROUP -> server.sendToGroup(messageBox);
                    default -> showIllegalStateException(messageBox);
                }
            }
            default -> showIllegalStateException(messageBox);
        }
    }

    private void workWithCommand(MessageBox messageBox) {
        switch (messageBox.getMessageTypeSecondLevel()) {
            case REQUEST -> workWithRequest(messageBox);
            case END -> {
                try {
                    closeConnection();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            default -> showIllegalStateException(messageBox);
        }
    }

    private void workWithRequest(MessageBox messageBox) {
        switch (messageBox.getMessageTypeThirdLevel()) {
            case REG -> workWithReg(messageBox);
            case AUTH -> workWithAuthPerson(messageBox);
            case LIST_ALL_PERSON -> workWithMapAll(messageBox);
        }
    }

    private void workWithMapAll(MessageBox messageBox) {
        List<Person> allPersonList = databaseHandling.getAllPersonList();
        switch (messageBox.getMessageTypeFourLevel()) {
            case PERSON ->
                    sendMessage(new MessageBox.Builder().buildCommandChangeMapAllPersonForPerson(allPersonList));
            case GROUP ->
                    sendMessage(new MessageBox.Builder().buildCommandChangeMapAllPersonForGroup(allPersonList));
        }
    }

    private void workWithReg(MessageBox messageBox) {
        switch (messageBox.getMessageTypeFourLevel()) {
            case PERSON -> workWithRegPerson(messageBox);
            case GROUP -> workWithRegGroup(messageBox);
        }
    }

    private void workWithRegPerson(MessageBox messageBox) {
        DBResponse dbResponse = databaseHandling.isPersonExistsInDatabase(
                messageBox.getPerson(),messageBox.getEmail()
        );
        if (dbResponse.isFlag()) {
            sendMessage(new MessageBox.Builder().buildCommandFailedRegPerson(dbResponse.getErrorOnFieldList()));
            return;
        }
        databaseHandling.registrationPerson(messageBox.getPerson(),messageBox.getEmail(),messageBox.getPassword());
        sendMessage(new MessageBox.Builder().buildCommandAcceptRegPerson());
    }

    private void workWithRegGroup(MessageBox messageBox) {
        if (databaseHandling.isGroupExistsInDatabase(messageBox.getGroup())) {
            sendMessage(new MessageBox.Builder().buildCommandFailedRegGroup());
            return;
        }
        databaseHandling.registrationGroup(messageBox.getGroup());
        sendMessage(new MessageBox.Builder().buildCommandAcceptRegGroup(messageBox.getGroup()));
    }

    private void workWithAuthPerson(MessageBox messageBox) {
        Person person = databaseHandling.authenticatePerson(
                messageBox.getEmail(), messageBox.getPassword()
        );
        if (Objects.isNull(person)) {
            sendMessage(new MessageBox.Builder().buildCommandFailedAuthPerson());
            return;
        }
        sendMessage(new MessageBox.Builder().buildCommandAcceptAuthPerson(person));
        this.myPerson = person;
        server.subscribe(myPerson, this);
    }

    public void transmissionTheHistoryOfCorrespondence(List<MessageBox> messageBoxesList) {
        try {
            sendMessage(new MessageBox.Builder().buildCommandRecoveryCorrespondenceHistory(messageBoxesList.size()));
            for (MessageBox messageBox : messageBoxesList) {
                out.writeObject(messageBox);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() throws IOException {
        if (nonNull(myPerson)) {
            server.unsubscribe(myPerson);
        }
        if (nonNull(in)) {
            in.close();
        }
        if (nonNull(out)) {
            out.close();
        }
        if (nonNull(socket)) {
            socket.close();
        }
    }

    private static void showIllegalStateException(MessageBox messageBox) {
        throw new IllegalStateException(String.format("Unexpected value: %s, %s, %s",
                messageBox.getMessageTypeFirstLevel(),
                messageBox.getMessageTypeSecondLevel(),
                messageBox.getMessageTypeThirdLevel()
        ));
    }

    public void sendGroupWhereIAmAMemberMap(List<Group> groupWhereIAmAMember) {
        sendMessage(new MessageBox.Builder().buildCommandChangeGroupIMemberGroup(groupWhereIAmAMember));
    }
}
