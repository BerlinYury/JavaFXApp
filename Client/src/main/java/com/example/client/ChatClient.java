package com.example.client;

import com.example.api.ResponseMessage;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
public class ChatClient implements IChatClient {

    private DataOutputStream out;
    private String nick;
    private ControllerClient controllerClient;
    private ControllerAuthenticate controllerAuthenticate;
    private ControllerRegistration controllerRegistration;

    public void setControllers(ControllerClient controllerClient,
                      ControllerAuthenticate controllerAuthenticate,
                      ControllerRegistration controllerRegistration) {
        this.controllerClient = controllerClient;
        this.controllerAuthenticate = controllerAuthenticate;
        this.controllerRegistration = controllerRegistration;
    }

    @Override
    public void openConnection() {
        ThreadManagerClient.getInstance().getExecutorService().execute(() -> {
            String LOCALHOST = "localhost";
            int PORT = 8129;
            try (Socket socket = new Socket(LOCALHOST, PORT);
                 DataInputStream in = new DataInputStream(socket.getInputStream());
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
                this.out = out;
                log.info(String.format("Клиент %s открыт", this.nick));
                while (true) {
                    final String messageStr = in.readUTF();
                    ResponseMessage message = ResponseMessage.createMessage(messageStr);
                    if (message == null) {
                        log.error(String.format("ResponseMessage for %s == null", this.nick));
                        continue;
                    }
                    switch (message.getType()) {
                        case RECOVERY -> {
                            controllerClient.appendOldMessages(message.getMessage());
                            System.out.println(message.getMessage());
                        }
                        case REG_OK -> {
                            controllerRegistration.onSuccess();
                            log.info(String.format("Клиент %s зарегистрирован", this.nick));
                        }
                        case REG_BUSY -> controllerRegistration.onBusy();
                        case AUTH_OK -> {
                            this.nick = message.getNick();
                            controllerAuthenticate.onSuccess();
                            log.info(String.format("Клиент %s авторизован", this.nick));
                        }
                        case AUTH_FAILED -> controllerAuthenticate.onError();
                        case AUTH_NICK_BUSY -> controllerAuthenticate.onBusy();
                        case AUTH_CHANGES -> controllerClient.addButtons(message.getClientsChangeList());
                        case RESPONSE -> controllerClient.addIncomingMessage(message);
                        default -> throw new IllegalStateException("Unexpected value: " + message.getType());
                    }
                }
            } catch (ConnectException cE) {
                System.out.println("Cервер не запущен");
                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNick() {
        return nick;
    }

}
