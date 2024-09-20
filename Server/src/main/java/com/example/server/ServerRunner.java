package com.example.server;

import lombok.Getter;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class ServerRunner {

    @Getter
    private static WeldContainer container;

    public static void main(String[] args) {
        Weld weld = new Weld();
        container = weld.initialize();
        container.select(ChatServer.class).get().run(args);
        container.shutdown();
    }
}
