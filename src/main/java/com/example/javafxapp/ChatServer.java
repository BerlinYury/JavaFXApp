package com.example.javafxapp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ControllerServer controller2;
    public static final String END = "/end";

    public ChatServer(ControllerServer controller2) {
        this.controller2 = controller2;
       connectionSocketServer();
    }

    public void connectionSocketServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(8593);
            System.out.println("Сервер ожидает подключения клиента");
            socket = serverSocket.accept();
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Клиент подключился");

            Thread threadServ = new Thread(() -> {
                try {
                    while (true) {
                        final String message = in.readUTF();
                        if (END.equals(message)) {
                            out.writeUTF(END);
                            break;
                        }
                        controller2.addMessage("Client: "+message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            });
            threadServ.start();
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
        System.out.println("Сервер закрыт");
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
