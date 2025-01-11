package com.example.server;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;




@ApplicationScoped
@NoArgsConstructor
@Getter
@Slf4j
public class CDIService {
    private DatabaseHandling databaseHandling;
    private CommandHandler commandHandler;
    private MessageHandler messageHandler;

    @Inject
    public CDIService(
            DatabaseHandling databaseHandling,
            MessageHandler messageHandler,
            CommandHandler commandHandler) {
      this.databaseHandling = databaseHandling;
      this.messageHandler = messageHandler;
      this.commandHandler = commandHandler;
    }

}
