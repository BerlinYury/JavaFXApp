package com.example.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public enum RequestType implements IRequestType {
    RETENTION("/retention") {
        @Override
        public RequestMessage createMessage(String message) {
            return new RequestMessage(RETENTION, message);
        }
    },
//(/retention msg)

    REGISTRATION("/registration") {
        @Override
        public RequestMessage createMessage(String loginAndPassword) {
            String[] split = loginAndPassword.split(" ", 2);
            String login = split[0];
            String password = split[1];
            return new RequestMessage(login, password, REGISTRATION);
        }
    },
    //(/registration login1 password1)

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

    @Getter
    private final String value;
    private static final Map<String, RequestType> VALUES;

    static {
        HashMap<String, RequestType> stringRequestTypeHashMap = new HashMap<>();
        for (RequestType fieldName : RequestType.values()) {
            stringRequestTypeHashMap.put(fieldName.getValue(), fieldName);
        }
        VALUES = Collections.unmodifiableMap(stringRequestTypeHashMap);
    }

    public static RequestType getRequestType(String msgPrefix) {
        return VALUES.get(msgPrefix);
    }


}
