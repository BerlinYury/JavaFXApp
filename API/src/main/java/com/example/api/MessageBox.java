package com.example.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class MessageBox implements Serializable {
    private final MessageType type;
    private final LocalDateTime dateTime;
    private final String message;
//    private final String senderNick;
//    private final String recipientNick;
//    private final String login;
//    private final String password;
//    private final int counterObj;
//    private final String[] clientsChangeList;
}
