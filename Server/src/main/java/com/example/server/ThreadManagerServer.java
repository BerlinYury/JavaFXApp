package com.example.server;

import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadManagerServer {
    private static ThreadManagerServer instance;
    @Getter
    private final ExecutorService executorService;

    private ThreadManagerServer() {
        executorService = Executors.newFixedThreadPool(10, run -> {
            Thread thread = new Thread(run);
            thread.setDaemon(true); // Устанавливаем поток как демон
            return thread;
        });
    }

    public static ThreadManagerServer getInstance() {
        return Objects.requireNonNullElseGet(instance, ThreadManagerServer::new);
    }

    public void shutdownMyExecutorService() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
