package com.example.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public enum ResponseType implements IResponseType {
    USER_ON("/userOn") {
        @Override
        public ResponseMessage createMessage(String nick) {
            return new ResponseMessage(USER_ON, nick);
        }
    },
    //(/userOn nick)

    USER_OFF("/userOff") {
        @Override
        public ResponseMessage createMessage(String nick) {
            return new ResponseMessage(USER_OFF, nick);
        }
    },
    //(/userOff nick)

    RECOVERY("/recovery") {
        @Override
        public ResponseMessage createMessage(String counterAndRecoveryMessages) {
            String[] split = counterAndRecoveryMessages.split(" ", 2);
            int counterObj = Integer.parseInt(split[0]);
            String oldMessages = split[1];
            return new ResponseMessage(RECOVERY, counterObj, oldMessages);
        }
    },
//(/recovery 12 msg)

    RESPONSE("/response") {
        @Override
        public ResponseMessage createMessage(String fromNickAndMessage) {
            String[] split = fromNickAndMessage.split(" ", 2);
            String fromNick = split[0];
            String message = split[1];
            return new ResponseMessage(RESPONSE, fromNick, message);
        }
    },
    //(/response nick3 hi bro)

    AUTH_OK("/authOK") {
        @Override
        public ResponseMessage createMessage(String nick) {
            return new ResponseMessage(AUTH_OK, nick);
        }
    },
    //(/authOK nick)

    AUTH_FAILED("/authFailed") {
        @Override
        public ResponseMessage createMessage(String emptyMsg) {
            return new ResponseMessage(AUTH_FAILED);
        }
    },
    //(/authFailed)

    AUTH_NICK_BUSY("/authNickBusy") {
        @Override
        public ResponseMessage createMessage(String emptyMsg) {
            return new ResponseMessage(AUTH_NICK_BUSY);
        }
    },
    //(/authNickBusy)

    REG_OK("/regOK") {
        @Override
        public ResponseMessage createMessage(String emptyMsg) {
            return new ResponseMessage(REG_OK);
        }
    },
    //(/regOK)
    REG_BUSY("/regBusy") {
        @Override
        public ResponseMessage createMessage(String emptyMsg) {
            return new ResponseMessage(REG_BUSY);
        }
    },
    //(/authNickBusy)

    AUTH_CHANGES("/changes") {
        @Override
        public ResponseMessage createMessage(String clientsChangeList) {
            String[] nicks = clientsChangeList.substring(
                    clientsChangeList.indexOf('[') + 1, clientsChangeList.indexOf(']')).split(",\\s*"
            );
            return new ResponseMessage(AUTH_CHANGES, nicks);
        }
    },
    //(/start)
    START_TRANSFER_OBJECTS("/start") {
        @Override
        public ResponseMessage createMessage(String emptyMsg) {
            return new ResponseMessage(START_TRANSFER_OBJECTS);
        }
    },
    //(/finish)
    FINISH_TRANSFER_OBJECTS("/finish") {
        @Override
        public ResponseMessage createMessage(String emptyMsg) {
            return new ResponseMessage(START_TRANSFER_OBJECTS);
        }
    };

    @Getter
    private final String value;
    private static final Map<String, ResponseType> VALUES;

    static {
        HashMap<String, ResponseType> stringResponseTypeHashMap = new HashMap<>();
        for (ResponseType fieldName : ResponseType.values()) {
            stringResponseTypeHashMap.put(fieldName.getValue(), fieldName);
        }
        VALUES = Collections.unmodifiableMap(stringResponseTypeHashMap);
    }

    public static ResponseType getResponseType(String msgPrefix) {
        return VALUES.get(msgPrefix);
    }
}
