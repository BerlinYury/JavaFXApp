package com.example.server;

import com.example.api.MessageBox;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


@Slf4j
@ServerEndpoint("/chat")
public class ChatServer {
    private final CDIService cdiService;
    @Getter
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    public ChatServer() {
        cdiService = CDI.current().select(CDIService.class).get();
    }

    @OnOpen
    public void onOpen(Session session) {
        log.info("Новое соединение: " + session.getId());
        sessions.add(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            MessageBox messageBox = MessageBox.fromJson(message);
            switch (messageBox.getMessageTypeFirstLevel()) {
                case COMMAND -> cdiService.getCommandHandler().handleCommand(messageBox, session);
                case MESSAGE -> cdiService.getMessageHandler().handleMessage(messageBox);
                default ->
                        throw new IllegalStateException("Unexpected value: " + messageBox.getMessageTypeFirstLevel());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @OnClose
    public void onClose(Session session) {
        log.info("Соединение закрыто: " + session.getId());
        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error(String.format("Ошибка на соединении: %s\n%s\n%s\n",
                session.getId(),
                throwable.getMessage(),
                Arrays.toString(throwable.getStackTrace())));
    }
}
