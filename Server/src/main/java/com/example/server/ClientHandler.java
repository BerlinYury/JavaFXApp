package com.example.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private final Socket socket;
    private final ChatServer server;
    private final DataInputStream in;
    private final DataOutputStream out;
    private String nick;

    //Поле нужно для того, чтобы можно было корректно выйти и закрыть окно на этапе авторизации.
    // Позволяет не заходить в цикл while метода readMessages
    private boolean isClientExit;


    public ClientHandler(Socket socket, ChatServer server) {
        try {
            isClientExit = false;
            this.nick = "";
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    authenticate();
                    readMessages();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод аутентификации клиента. Ожидает ввода логина и пароля, проверяет их на валидность и авторизует клиента.
     * В случае успеха отправляет клиенту сообщение об успешной авторизации и добавляет его в список подключенных клиентов.
     * Если никнейм уже занят, отправляет клиенту сообщение о неудачной авторизации.
     * Если логин и/или пароль введены неверно, отправляет клиенту сообщение о неудачной авторизации.
     */
    private void authenticate() {
        while (true) {
            try {
                String str = in.readUTF();
                if (isClientExit(str)) {
                    isClientExit = true;//Поле нужно для того, чтобы можно было корректно выйти и закрыть окно на этапе авторизации
                    break;
                }
                if (str.startsWith(Constants.AUTH.getValue())) {
                    String nick = server.getAuthService().authenticate(str);
                    if (nick == null) {
                        sendMessage(Constants.AUTH_FAILED.getValue());
                        continue;
                    }
                    if (server.isNickBusy(nick)) {
                        sendMessage(Constants.AUTH_NICK_BUSY.getValue());
                        continue;
                    }
                    this.nick = nick;
                    sendMessage(String.format("%s %s", Constants.AUTH_OK.getValue(), nick));
                    String msg = String.format("%s вошёл в чат =)!", Constants.SEND_TO_ALL.getValue());
                    server.selectiveSendMessage(msg, this);
                    server.subscribe(this);
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Метод отправки сообщения клиенту. Отправляет сообщение через исходящий поток клиента.
     *
     * @param message сообщение для отправки клиенту
     */
    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод чтения входящих сообщений от клиента. Принимает сообщение, проверяет его на наличие команды завершения
     * чата, и если команда присутствует, закрывает соединение с клиентом. В противном случае отправляет сообщение
     * всем клиентам или выборочно, через метод selectiveSendMessage.
     */
    private void readMessages() {
        try {
            while (!isClientExit) {
                String message = in.readUTF();
                if (isClientExit(message)) {
                    break;
                }
                server.selectiveSendMessage(message, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isClientExit(String message) throws IOException {
        if (Constants.END.getValue().equals(message)) {
            out.writeUTF(Constants.END.getValue());
            return true;
        }
        return false;
    }

    /**
     * Метод для закрытия соединения с клиентом и освобождения ресурсов.
     * Если соединение еще не закрыто, отправляет всем подключенным клиентам сообщение
     * о том, что текущий клиент отключился, и удаляет его из списка подключенных.
     */
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
                String msgExitClient = String.format("%s Пользователь отключился!", Constants.SEND_TO_ALL.getValue());
                server.selectiveSendMessage(msgExitClient, this);
                server.unsubscribe(this);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Клиент отключился от сервера");
    }

    public String getNick() {
        return nick;
    }

    /**
     * Переопределение метода toString для возврата ника клиента в виде строки.
     */
    @Override
    public String toString() {
        return nick;
    }
}
