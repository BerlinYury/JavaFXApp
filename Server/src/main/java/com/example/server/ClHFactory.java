package com.example.server;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class ClHFactory {

    @Produces
    public static ClientHandler factory() {
        ChatServer server = ServerRunner.getContainer().select(ChatServer.class).get();
        DatabaseHandling databaseHandling =ServerRunner.getContainer().select(DatabaseHandling.class).get();
        return new ClientHandler(server,databaseHandling);
    }
}
