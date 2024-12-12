package com.example.client;

import com.example.api.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class ChatClient extends WebSocketClient {
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
    ) throws URISyntaxException {
        super(new URI("ws://localhost:8129")); // URI WebSocket-сервера
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

    @Override
    public void onMessage(String message) {
        try {
            MessageBox messageBox = MessageBox.fromJson(message);
            switch (messageBox.getMessageTypeFirstLevel()) {
                case COMMAND -> workWithCommand(messageBox);
                case MESSAGE -> workWithMessage(messageBox);
                default -> showIllegalStateException(messageBox);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void workWithMessage(MessageBox messageBox) {
        if (messageBox.isHistory()){
            addMessageToMap(messageBox);
        }else {
            switch (messageBox.getMessageTypeSecondLevel()) {
                case INCOMING -> controllerClient.addIncomingMessage(messageBox);
                default -> showIllegalStateException(messageBox);
            }
        }
    }

    private void workWithCommand(MessageBox messageBox) {
        switch (messageBox.getMessageTypeSecondLevel()) {
            case ACCEPT -> workWithAccept(messageBox);
            case FAILED -> workWithFailed(messageBox);
            case CHANGE -> workWithChange(messageBox);
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
            send(messageBox.toJson());
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

    private static void showIllegalStateException(MessageBox messageBox) {
        throw new IllegalStateException(String.format("Unexpected value: %s, %s, %s",
                messageBox.getMessageTypeFirstLevel(),
                messageBox.getMessageTypeSecondLevel(),
                messageBox.getMessageTypeThirdLevel()
        ));
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("Соединение установлено");
        log.info("Соединение установлено");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("Соединение закрыто: {} - {}", code, reason);
    }

    @Override
    public void onError(Exception ex) {
        log.error("Ошибка соединения", ex);
    }
}
