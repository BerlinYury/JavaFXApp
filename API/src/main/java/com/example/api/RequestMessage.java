package com.example.api;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class RequestMessage {
    private final String message;
    private final RequestType type;
    private final String nick;
    private final String login;
    private final String password;

    public RequestMessage(String login, String password, RequestType type) {
        this.type = type;
        this.login = login;
        this.password = password;
        this.nick = null;
        this.message = null;
    }

    public RequestMessage(RequestType type) {
        this.type = type;
        this.login = null;
        this.password = null;
        this.nick = null;
        this.message = null;
    }

    public RequestMessage(RequestType type, String nick, String message) {
        this.message = message;
        this.type = type;
        this.nick = nick;
        this.login = null;
        this.password = null;
    }

    public RequestMessage(RequestType type, String message) {
        this.message = message;
        this.type = type;
        this.nick = null;
        this.login = null;
        this.password = null;
    }

    public static RequestMessage createMessage(String msg) {
        String[] partsOfMessage = msg.split(" ", 2);
        String prefix = partsOfMessage[0];
        RequestType requestType = RequestType.getRequestType(prefix);

        if (requestType == null) {
            log.error("requestType == null");
            throw new NullPointerException("requestType == null");
        }
        switch (requestType) {
            case END -> {
                return requestType.createMessage(null);
            }
            default -> {
                return requestType.createMessage(partsOfMessage[1]);
            }
        }
    }

}
