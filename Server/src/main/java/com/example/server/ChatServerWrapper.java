package com.example.server;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.InetSocketAddress;

@ApplicationScoped
public class ChatServerWrapper {
    private final ChatServer chatServer;

    @Inject
    public ChatServerWrapper(DatabaseHandling databaseHandling) {
        this.chatServer = new ChatServer(databaseHandling);
    }

    public ChatServer getChatServer() {
        return chatServer;
    }
}