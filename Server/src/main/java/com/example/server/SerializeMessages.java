package com.example.server;

import com.example.api.RequestMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.Files.*;

@Slf4j
public abstract class SerializeMessages {
    public static final int maxHistoryMessagesLines = 100;

    public static String readMessagesFromFile(String nameOfFile) {
        Path path = createPath(nameOfFile);
        isDirectoryExists(path);
        isFileExists(path);
        try {
           String messages = readString(path);
            log.info("Архив сообщений прочитан");
            return messages;
        } catch (IOException e) {
            log.error("Не удалось прочитать архив сообщений");
            throw new RuntimeException(e);
        }
    }

    public static void writeMessagesToFile(RequestMessage newPartMessage, String nameOfFile) {
        Path path = createPath(nameOfFile);
        isDirectoryExists(path);
        isFileExists(path);
        try {
            writeString(path, newPartMessage.getMessage());
            checkForExceedingTheLimitOf100Lines(path);
            log.info("Сообщения записаны в архив");
        } catch (IOException e) {
            log.error("Не удалось записать сообщения в архив");
            throw new RuntimeException(e);
        }
    }

    private static void checkForExceedingTheLimitOf100Lines(Path path) throws IOException {
        List<String> historyMessagesLines = readAllLines(path);
        if (historyMessagesLines.size() > maxHistoryMessagesLines) {
            write(path, (Iterable<String>) historyMessagesLines.stream()
                    .skip(historyMessagesLines.size() - maxHistoryMessagesLines)::iterator);
        }
    }

    private static void isFileExists(Path path) {
        if (!exists(path)) {
            try {
                createFile(path);
                log.info("Файл для архива создан");
            } catch (IOException e) {
                log.error("Не удалось создать файл архива");
                throw new RuntimeException(e);
            }
        }
    }

    private static void isDirectoryExists(Path path) {
        Path parent = path.getParent();
        if (!exists(parent)) {
            try {
                createDirectories(parent);
                log.info("Директория для архива создана");
            } catch (IOException e) {
                log.error("Не удалось создать директорию");
                throw new RuntimeException(e);
            }
        }
    }

    private static Path createPath(String nameOfFile) {
        String nameOfFileAndExtension = String.format("%s.txt", nameOfFile);
        return Path.of("Server", "src", "main", "resources", "Files", nameOfFileAndExtension);
    }

}
