package com.example.client;

import com.example.api.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.*;

@Slf4j
public class ChatClient {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private final ControllerClient controllerClient;
    private final ControllerAuthenticate controllerAuthenticate;
    private final ControllerRegistrationPerson controllerRegistrationPerson;
    @Getter
    private final ConcurrentHashMap<String, Correspondence> correspondenceMap;
    private ControllerCreateChat controllerCreateChat;
    private ControllerCreateGroup controllerCreateGroup;

    public ChatClient(ControllerClient controllerClient,
                      ControllerAuthenticate controllerAuthenticate,
                      ControllerRegistrationPerson controllerRegistrationPerson
    ) {
        this.controllerClient = controllerClient;
        this.controllerAuthenticate = controllerAuthenticate;
        this.controllerRegistrationPerson = controllerRegistrationPerson;
        this.correspondenceMap = new ConcurrentHashMap<>();
    }

    public void setControllerCreateChat(ControllerCreateChat controllerCreateChat) {
        this.controllerCreateChat = controllerCreateChat;
    }

    public void setControllerCreateGroup(ControllerCreateGroup controllerCreateGroup) {
        this.controllerCreateGroup = controllerCreateGroup;
    }

    public void openConnection() {
        ThreadManagerClient.getInstance().getExecutorService().execute(() -> {
            try {
                String LOCALHOST = "localhost";
                int PORT = 8129;
                socket = new Socket(LOCALHOST, PORT);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                readMessage();
            } catch (ConnectException cE) {
                System.out.println("Cервер не запущен");
                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    closeConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void readMessage() {
        try {
            while (true) {
                MessageBox messageBox = (MessageBox) in.readObject();
                switch (messageBox.getMessageTypeFirstLevel()) {
                    case COMMAND -> workWithCommand(messageBox);
                    case MESSAGE -> workWithMessage(messageBox);
                    default -> showIllegalStateException(messageBox);
                }
            }
        } catch (EOFException e) {
            System.out.println(e.getClass().getName());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void workWithMessage(MessageBox messageBox) {
        switch (messageBox.getMessageTypeSecondLevel()) {
            case INCOMING -> controllerClient.addIncomingMessage(messageBox);
            default -> showIllegalStateException(messageBox);
        }
    }

    private void workWithCommand(MessageBox messageBox) {
        switch (messageBox.getMessageTypeSecondLevel()) {
            case ACCEPT -> workWithAccept(messageBox);
            case FAILED -> workWithFailed(messageBox);
            case CHANGE -> workWithChange(messageBox);
            case RECOVERY -> workWithRecovery(messageBox);
            default -> showIllegalStateException(messageBox);
        }
    }

    private void workWithAccept(MessageBox messageBox) {
        switch (messageBox.getMessageTypeThirdLevel()) {
            case REG -> {
                switch (messageBox.getMessageTypeFourLevel()) {
                    case PERSON -> controllerRegistrationPerson.onAcceptRegistrationPerson();
                    case GROUP -> controllerCreateGroup.onAcceptRegistrationGroup(messageBox);
                    default -> showIllegalStateException(messageBox);
                }
            }
            case AUTH -> {
                switch (messageBox.getMessageTypeFourLevel()) {
                    case PERSON -> {
                        Controller.myPerson = messageBox.getOwner();
                        controllerAuthenticate.onAcceptAuthenticatePerson();
                    }
                    default -> showIllegalStateException(messageBox);
                }
            }
            default -> showIllegalStateException(messageBox);
        }
    }

    private void workWithFailed(MessageBox messageBox) {
        switch (messageBox.getMessageTypeThirdLevel()) {
            case REG -> {
                switch (messageBox.getMessageTypeFourLevel()) {
                    case PERSON ->
                            controllerRegistrationPerson.onFailedRegistrationPerson(messageBox.getErrorOnFieldList());
                    case GROUP -> controllerCreateGroup.onFailedRegistrationGroup();
                    default -> showIllegalStateException(messageBox);
                }
            }
            case AUTH -> {
                switch (messageBox.getMessageTypeFourLevel()) {
                    case PERSON -> controllerAuthenticate.onFailedAuthenticatePerson();
                    default -> showIllegalStateException(messageBox);
                }
            }
            default -> showIllegalStateException(messageBox);
        }

    }

    private void workWithRecovery(MessageBox messageBox) {
        switch (messageBox.getMessageTypeThirdLevel()) {
            case CORRESPONDENCE_HISTORY -> deserializeMessages(messageBox.getCounterUnit());
            default -> showIllegalStateException(messageBox);
        }
    }


    private void workWithChange(MessageBox messageBox) {
        switch (messageBox.getMessageTypeThirdLevel()) {
            case STATUS -> controllerClient.updatePersonStatus(messageBox.getActivePersonList());
            case LIST_ALL_PERSON -> {
                switch (messageBox.getMessageTypeFourLevel()) {
                    case PERSON -> controllerCreateChat.addAllPersonList(messageBox.getAllPersonList());
                    case GROUP -> controllerCreateGroup.addAllPersonList(messageBox.getAllPersonList());
                }
            }
            case LIST_GROUP_I_MEMBER ->
                messageBox.getGroupWhereIAmAMemberList().forEach(group->{
                    if (!correspondenceMap.containsKey(group.getId())){
                        controllerClient.addNewUnitToMap(
                                new Correspondence(group.getId(),group,CorrespondenceType.GROUP,new ArrayList<>()),
                                false
                        );
                    }
                });
            default -> showIllegalStateException(messageBox);
        }
    }

    public void sendMessage(MessageBox messageBox) {
        try {
            out.writeObject(messageBox);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deserializeMessages(int counterObj) {
        try {
            for (int i = 0; i < counterObj; i++) {
                MessageBox messageBox = (MessageBox) in.readObject();
                addMessageToMap(messageBox);
            }
        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Correspondence addMessageToMap(MessageBox messageBox) {
        Correspondence correspondence;
        switch (messageBox.getMessageTypeFourLevel()) {
            case PERSON -> {
                Person person = messageBox.getPerson();
                String personId = person.getId();
                if (correspondenceMap.containsKey(personId)){
                    correspondence = correspondenceMap.get(personId);
                }else {
                     correspondence = new Correspondence(personId, person,
                            CorrespondenceType.PERSON,new ArrayList<>());
                    correspondenceMap.put(personId, correspondence);
                    controllerClient.addButtonsForCorrespondence(correspondence);
                }
                correspondence.getMessageBoxList().add(messageBox);
            }
            case GROUP -> {
                Group group = messageBox.getGroup();
                String groupId = group.getId();
                if (correspondenceMap.containsKey(groupId)){
                    correspondence = correspondenceMap.get(groupId);
                }else {
                     correspondence = new Correspondence(groupId, group,
                            CorrespondenceType.GROUP,new ArrayList<>());
                    correspondenceMap.put(groupId, correspondence);
                    controllerClient.addButtonsForCorrespondence(correspondence);
                }
                correspondence.getMessageBoxList().add(messageBox);
            }
            default -> throw new IllegalStateException();
        }
        return correspondence;
    }

    private void closeConnection() throws IOException {
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
}
