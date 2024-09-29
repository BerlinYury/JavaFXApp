package com.example.api;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class ResponseMessage {
    private final String[] clientsChangeList;
    private final ResponseType type;
    private final String nick;
    private final String message;
    private final String fromNick;
    private final int counterObj;


    public ResponseMessage(ResponseType type, String[] clientsChangeList) {
        this.clientsChangeList = clientsChangeList;
        this.type = type;
        this.nick = null;
        this.message = null;
        this.fromNick = null;
        this.counterObj = 0;
    }

    public ResponseMessage(ResponseType type, String fromNick, String message) {
        this.type = type;
        this.fromNick = fromNick;
        this.message = message;
        this.nick = null;
        this.clientsChangeList = null;
        this.counterObj = 0;
    }

    public ResponseMessage(ResponseType type, String nick) {
        this.type = type;
        this.nick = nick;
        this.clientsChangeList = null;
        this.message = null;
        this.fromNick = null;
        this.counterObj = 0;
    }

    public ResponseMessage(ResponseType type, int counterObj, String message) {
        this.type = type;
        this.nick = null;
        this.clientsChangeList = null;
        this.message = message;
        this.fromNick = null;
        this.counterObj = counterObj;
    }

    public ResponseMessage(ResponseType type) {
        this.type = type;
        this.nick = null;
        this.clientsChangeList = null;
        this.message = null;
        this.fromNick = null;
        this.counterObj = 0;
    }

    public static ResponseMessage createMessage(String msg) {
        String[] partsOfMessage = msg.split(" ", 2);
        String prefix = partsOfMessage[0];
        ResponseType responseType = ResponseType.getResponseType(prefix);

        if (responseType == null) {
            log.error("responseType == null");
            throw new NullPointerException("responseType == null");
        }
        switch (responseType) {
            case AUTH_FAILED, AUTH_NICK_BUSY, REG_OK, REG_BUSY, START_TRANSFER_OBJECTS, FINISH_TRANSFER_OBJECTS -> {
                return responseType.createMessage(null);
            }
            default -> {
                return responseType.createMessage(partsOfMessage[1]);
            }
        }
    }

}
