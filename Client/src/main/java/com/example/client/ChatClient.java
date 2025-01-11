package com.example.client;

import com.example.api.*;
import jakarta.websocket.*;
import javafx.application.Platform;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.ConnectException;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@ClientEndpoint
public class ChatClient  {
    private final ControllerClient controllerClient;
    private final ControllerAuthenticate controllerAuthenticate;
    private final ControllerRegistrationPerson controllerRegistrationPerson;
    @Getter
    private final ConcurrentHashMap<String, Correspondence> correspondenceMap;
    private ControllerCreateChat controllerCreateChat;
    private ControllerCreateGroup controllerCreateGroup;
    @Getter
    private Person myPerson;
    private Session session;

    public ChatClient(ControllerClient controllerClient,
                      ControllerAuthenticate controllerAuthenticate,
                      ControllerRegistrationPerson controllerRegistrationPerson) {
        this.controllerClient = controllerClient;
        this.controllerAuthenticate = controllerAuthenticate;
        this.controllerRegistrationPerson = controllerRegistrationPerson;
        this.correspondenceMap = new ConcurrentHashMap<>();
    }

    public void connect(String serverUri) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, new URI(serverUri));
    }
    public void setControllerCreateChat(ControllerCreateChat controllerCreateChat) {
        this.controllerCreateChat = controllerCreateChat;
    }

    public void setControllerCreateGroup(ControllerCreateGroup controllerCreateGroup) {
        this.controllerCreateGroup = controllerCreateGroup;
    }

    @OnMessage
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
        var messageType = messageBox.getMessageTypeSecondLevel();
        switch (messageType) {
            case ACCEPT -> workWithAccept(messageBox);
            case FAILED -> workWithFailed(messageBox);
            case CHANGE -> workWithChange(messageBox);
            case END -> {
                stopClient();
                closeAllResources();
                Platform.exit();
            }
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
                        this.myPerson= messageBox.getOwner();
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

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Соединение установлено");
    }

    public void stopClient() {
        try {
            sendMessage(new MessageBox.Builder().buildCommandEnd());
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            log.error("Ошибка при закрытии клиента", e);
        }
    }

    public void closeAllResources(){
        ThreadManagerClient.getInstance().shutdownMyExecutorService();
        ThreadManagerClient.getInstance().shutdownMyScheduledExecutorService();
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Соединение закрыто: " + closeReason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        if (throwable instanceof ConnectException) {
            System.out.println("Сервер не запущен.");
            Platform.exit(); // Завершение приложения JavaFX
        } else {
            throwable.printStackTrace();
        }
        log.error("Ошибка соединения", throwable);
    }
    public void sendMessage(MessageBox messageBox) {
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(messageBox.toJson());
        }
    }
}
