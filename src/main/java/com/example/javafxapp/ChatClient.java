package com.example.javafxapp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ControllerClient controller;

    public ChatClient(ControllerClient controller) {
        this.controller = controller;
        connectionSocket();
    }

    public void connectionSocket() {
        try {
            socket = new Socket("localhost", 8593);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            Thread threadClient = new Thread(() -> {
                try {
                    while (true) {
                        final String message = in.readUTF();
                        if (ChatServer.END.equals(message)) {
                            out.writeUTF(ChatServer.END);
                            break;
                        }
                        controller.addMessage("Server: "+message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            });
            threadClient.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        System.out.println("Клиент закрыт");
        System.exit(0);
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
