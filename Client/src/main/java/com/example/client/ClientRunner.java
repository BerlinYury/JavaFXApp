package com.example.client;

import lombok.Getter;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class ClientRunner {

    @Getter
    private static WeldContainer container;

    public static void main(String[] args) {
        Weld weld = new Weld();
        container = weld.initialize();
        UIClient.go(args);
        container.shutdown();
    }
}
