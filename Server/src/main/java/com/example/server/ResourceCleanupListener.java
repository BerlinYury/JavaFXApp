package com.example.server;

import ch.qos.logback.classic.LoggerContext;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.sql.Driver;

import java.io.IOException;
import java.sql.DriverManager;
import java.util.Enumeration;


@WebListener
@Slf4j
public class ResourceCleanupListener implements ServletContextListener {
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Метод closeAllResources() вызван.");
        try {
            // Остановка пользовательского менеджера потоков
            ThreadManagerServer.getInstance().shutdownMyExecutorService();
            log.info("Менеджер потоков остановлен.");

            for (Session session : ChatServer.getSessions()) {
                try {
                    if (session.isOpen()) {
                        try {
                            session.close();
                        }catch (IOException e) {
                            log.error("Error closing session: {}", e.getMessage(), e);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Enumeration<Driver> drivers = DriverManager.getDrivers();
            if (!drivers.hasMoreElements()){
                log.warn("Драйвер не обнаружен");
            }
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                try {
                    DriverManager.deregisterDriver(driver);
                   log.debug("Драйвер JDBC успешно удален: " + driver);
                } catch (Exception e) {
                    log.error("Ошибка при удалении драйвера JDBC: " + driver);
                    e.printStackTrace();
                }
            }

            // Остановка потока AbandonedConnectionCleanupThread
            try {
                AbandonedConnectionCleanupThread.checkedShutdown();
                log.info("AbandonedConnectionCleanupThread успешно завершён.");
            } catch (Exception e) {
                log.error("Ошибка при завершении AbandonedConnectionCleanupThread: " + e.getMessage());
                Thread.currentThread().interrupt(); // Восстановить флаг прерывания
            }
        } catch (Exception e) {
            log.error("Ошибка при освобождении ресурсов: " + e.getMessage(), e);
        }

        // Остановка Logback
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.stop();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Приложение запущено.");
    }
}