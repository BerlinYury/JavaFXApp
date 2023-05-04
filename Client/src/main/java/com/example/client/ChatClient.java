package com.example.client;

import com.example.api.ResponseMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final ControllerClient controller;
    private ControllerAuthenticate controllerAuthenticate;
    private String nick;

    public ChatClient(ControllerClient controller) {
        this.controller = controller;
    }

    public void openConnection() {
        try {
            String LOCALHOST = "localhost";
            int PORT = 8129;
            socket = new Socket(LOCALHOST, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            Thread threadClient = new Thread(() -> {
                try {
                    while (true) {
                        final String messageStr = in.readUTF();
                        ResponseMessage message = ResponseMessage.createMessage(messageStr);
                        if (message == null) {
                            //TODO: Добавть логирование
                            continue;
                        }
                        switch (message.getType()) {
                            case AUTH_OK:
                                this.nick = message.getNick();
                                controllerAuthenticate.onSuccess();
                                break;
                            case AUTH_FAILED:
                                controllerAuthenticate.onError();
                                break;
                            case AUTH_NICK_BUSY:
                                controllerAuthenticate.onBusy();
                                break;
                            case AUTH_CHANGES:
                                controller.addButtons(message.getClientsChangeList());
                                break;
                            case RESPONSE:
                                controller.addIncomingMessage(message);
                                break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            });
            threadClient.setDaemon(true);
            threadClient.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Клиент закрыт");
    }

    public String getNick() {
            return nick;
    }
    public void setControllerAuthenticate(ControllerAuthenticate controllerAuthenticate){
        this.controllerAuthenticate=controllerAuthenticate;
    }
}
