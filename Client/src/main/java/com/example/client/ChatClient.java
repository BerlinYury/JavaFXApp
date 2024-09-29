package com.example.client;

import com.example.api.MessageBox;
import com.example.api.RequestType;
import com.example.api.ResponseMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.*;

@Slf4j
public class ChatClient implements IChatClient {

    private Socket textSocket;
    private Socket objectSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private ObjectInputStream inObj;
    private ObjectOutputStream outObj;
    @Getter
    private String nick;
    private final ControllerClient controllerClient;
    private final ControllerAuthenticate controllerAuthenticate;
    private final ControllerRegistration controllerRegistration;
    @Getter
    private final List<MessageBox> messageSession = new ArrayList<>();
    private static final Object mon = new Object();

    public ChatClient(ControllerClient controllerClient,
                      ControllerAuthenticate controllerAuthenticate,
                      ControllerRegistration controllerRegistration) {
        this.controllerClient = controllerClient;
        this.controllerAuthenticate = controllerAuthenticate;
        this.controllerRegistration = controllerRegistration;
    }

    @Override
    public void openConnection() {
        ThreadManagerClient.getInstance().getExecutorService().execute(() -> {
            try {
                String LOCALHOST = "localhost";
                int PORT = 8129;

                textSocket = new Socket(LOCALHOST, PORT);
                objectSocket = new Socket(LOCALHOST, PORT);

                in = new DataInputStream(textSocket.getInputStream());
                out = new DataOutputStream(textSocket.getOutputStream());
                outObj = new ObjectOutputStream(objectSocket.getOutputStream());
                inObj = new ObjectInputStream(objectSocket.getInputStream());

                readMessage();

            } catch (ConnectException cE) {
                System.out.println("Cервер не запущен");
                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    closeConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void readMessage() {
        try {
            log.info(String.format("Клиент %s открыт", this.nick));
            while (true) {
                ResponseMessage message = ResponseMessage.createMessage(in.readUTF());
                if (isNull(message)) {
                    log.error(String.format("ResponseMessage for %s == null", this.nick));
                    continue;
                }
                switch (message.getType()) {
                    case RECOVERY -> deserializeMessages(message);
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
                    case RESPONSE, USER_ON, USER_OFF -> controllerClient.addIncomingMessage(message);
                    case START_TRANSFER_OBJECTS, FINISH_TRANSFER_OBJECTS -> {
                        synchronized (mon) {
                            mon.notify();
                        }
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + message.getType());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deserializeMessages(ResponseMessage message) {
        List<MessageBox> oldMessageSession = new ArrayList<>();
        ThreadManagerClient.getInstance().getExecutorService().execute(() -> {
            try {
                sendMessage(RequestType.START_TRANSFER_OBJECTS.getValue());
                for (int i = 0; i < message.getCounterObj(); i++) {
                    Object messageBox = inObj.readObject();
                    if (messageBox instanceof MessageBox) {
                        oldMessageSession.add((MessageBox) messageBox);
                    } else {
                        log.error("Объект не соответствует типу MessageBox");
                        throw new IllegalArgumentException("Объект не соответствует типу " +
                                "MessageBox");
                    }
                }
                sendMessage(RequestType.FINISH_TRANSFER_OBJECTS.getValue());
                controllerClient.appendOldMessages(oldMessageSession);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    public void serializeMessages() throws IOException, InterruptedException {
        sendMessage(String.format("%s %d %s", RequestType.RETENTION.getValue(), messageSession.size(),
                "text"));
        synchronized (mon) {
            mon.wait();
            for (MessageBox messageBox : messageSession) {
                outObj.writeObject(messageBox);
            }
            messageSession.clear();
            mon.wait();
        }
    }


    private void closeConnection() throws IOException {
        if (nonNull(in)) {
            in.close();
        }
        if (nonNull(out)) {
            out.close();
        }
        if (nonNull(inObj)) {
            inObj.close();
        }
        if (nonNull(outObj)) {
            outObj.close();
        }
        if (nonNull(objectSocket)) {
            objectSocket.close();
        }
        if (nonNull(textSocket)) {
            textSocket.close();
        }
    }
}
