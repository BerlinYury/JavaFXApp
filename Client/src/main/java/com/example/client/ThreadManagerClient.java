package com.example.client;

import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.*;

public class ThreadManagerClient {
    private volatile static ThreadManagerClient instance;
    @Getter
    private final ExecutorService executorService;
    @Getter
    private final ScheduledExecutorService scheduledExecutorService;

    private ThreadManagerClient() {
        executorService = Executors.newFixedThreadPool(10, this::treadFactoryRunnable);
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(this::treadFactoryRunnable);
    }

    private Thread treadFactoryRunnable(Runnable run) {
        Thread thread = new Thread(run);
        thread.setDaemon(true);
        return thread;
    }

    public static ThreadManagerClient getInstance() {
        if (Objects.isNull(instance)) {
            synchronized (ThreadManagerClient.class) {
                return Objects.requireNonNullElseGet(instance, ThreadManagerClient::new);
            }
        } else {
            return instance;
        }
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

    public void shutdownMyScheduledExecutorService() {
        scheduledExecutorService.shutdown();
        try {
            if (!scheduledExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduledExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduledExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
