package com.example.api;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@ToString
public class MessageBox implements Serializable {
    private final MessageType type;
    private final LocalDateTime dateTime;
    private final String message;
    private final CommandType commandType;
    private final String senderNick;
    private final String recipientNick;
    private final String login;
    private final String password;
    private final int counterObj;
    private final String[] clientsChangeList;

    private MessageBox(Builder builder) {
        this.type = builder.type;
        this.dateTime = builder.dateTime;
        this.message = builder.message;
        this.commandType = builder.commandType;
        this.senderNick = builder.senderNick;
        this.recipientNick = builder.recipientNick;
        this.login = builder.login;
        this.password = builder.password;
        this.counterObj = builder.counterObj;
        this.clientsChangeList = builder.clientsChangeList;
    }

    @NoArgsConstructor
    public static class Builder {
        private MessageType type;
        private LocalDateTime dateTime;
        private String message;
        private CommandType commandType;
        private String senderNick;
        private String recipientNick;
        private String login;
        private String password;
        private int counterObj;
        private String[] clientsChangeList;

        public Builder type(MessageType type) {
            this.type = type;
            return this;
        }

        public Builder dateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder commandType(CommandType commandType) {
            this.commandType = commandType;
            return this;
        }

        public Builder senderNick(String senderNick) {
            this.senderNick = senderNick;
            return this;
        }

        public Builder recipientNick(String recipientNick) {
            this.recipientNick = recipientNick;
            return this;
        }

        public Builder login(String login) {
            this.login = login;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder counterObj(int counterObj) {
            this.counterObj = counterObj;
            return this;
        }

        public Builder clientsChangeList(String[] clientsChangeList) {
            this.clientsChangeList = clientsChangeList;
            return this;
        }
        public MessageBox build(){
           return new MessageBox(this);
        }
    }
}
