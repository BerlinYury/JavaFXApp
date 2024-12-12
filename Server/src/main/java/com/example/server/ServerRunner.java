package com.example.server;

import lombok.Getter;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;


public class ServerRunner {
    @Getter
    private static WeldContainer container;
    @Getter
    private static ChatServer chatServer;


    public static void main(String[] args) {
        Weld weld = new Weld();
        container = weld.initialize();
        chatServer = container.select(ChatServerWrapper.class).get().getChatServer();
        chatServer.run(args);
    }
}
