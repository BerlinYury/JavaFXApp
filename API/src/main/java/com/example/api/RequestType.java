package com.example.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum RequestType {

    END("/end") {
        @Override
        public RequestMessage createMessage(String emptyMessage) {
            return new RequestMessage(END);
        }
    },
    //(/end)
    AUTH("/auth") {
        @Override
        public RequestMessage createMessage(String loginAndPassword) {
            String[] split = loginAndPassword.split(" ", 2);
            String login = split[0];
            String password = split[1];
            return new RequestMessage(login, password, AUTH);
        }
    },
    // (/auth login password)
    SEND_TO_ONE("/toOne") {
        @Override
        public RequestMessage createMessage(String nickAndMessage) {
            String[] split = nickAndMessage.split(" ", 2);
            String nick = split[0];
            String message = split[1];
            return new RequestMessage(SEND_TO_ONE, nick, message);
        }
    },
    // (/toOne nick msg)
    SEND_TO_ALL("/toAll") {
        @Override
        public RequestMessage createMessage(String message) {
            return new RequestMessage(SEND_TO_ALL, message);
        }
    };
    // (/toAll msg)

    private static final Map<String, RequestType> VALUES;
    static {
        HashMap<String, RequestType> stringRequestTypeHashMap = new HashMap<>();
        for (RequestType fieldName : RequestType.values()) {
            stringRequestTypeHashMap.put(fieldName.getValue(), fieldName);
        }
        VALUES = Collections.unmodifiableMap(stringRequestTypeHashMap);
    }

    private final String value;

    RequestType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public abstract RequestMessage createMessage(String splitMessageSecondPart);

    public static RequestType getRequestType(String msgPrefix){
        return VALUES.get(msgPrefix);
    }


}
