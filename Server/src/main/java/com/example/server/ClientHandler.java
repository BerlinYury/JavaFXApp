package com.example.server;

import com.example.api.RequestMessage;
import com.example.api.RequestType;
import com.example.api.ResponseType;

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

    public ClientHandler(Socket socket, ChatServer server) {
        try {
            this.nick = "";
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    if (isClientAuth()) {
                        readMessages();
                    }
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
    private boolean isClientAuth() {
        while (true) {
            try {
                String messageStr = in.readUTF();
                RequestMessage message = RequestMessage.createMessage(messageStr);
                if (message == null) {
                    //TODO: Добавть логирование
                    continue;
                }
                switch (message.getType()) {
                    case END:
                        out.writeUTF(RequestType.END.getValue());
                        return false;
                    case AUTH:
                        String nick = server.getAuthService().authenticate(message.getLogin(), message.getPassword());
                        if (nick == null) {
                            sendMessage(ResponseType.AUTH_FAILED.getValue());
                            continue;
                        }
                        if (server.isNickBusy(nick)) {
                            sendMessage(ResponseType.AUTH_NICK_BUSY.getValue());
                            continue;
                        }
                        this.nick = nick;
                        sendMessage(String.format("%s %s", ResponseType.AUTH_OK.getValue(), nick));
                        sendMessageAboutChangingCustomerStatus("вошёл в чат =)!");
                        server.subscribe(nick, this);
                        return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessageAboutChangingCustomerStatus(String msg) throws IOException{
        RequestMessage messageChangingCustomerStatus = RequestMessage.createMessage(
                String.format("%s %s",RequestType.SEND_TO_ALL.getValue(),msg)
                );
        if (messageChangingCustomerStatus != null) {
            server.sendToAll(messageChangingCustomerStatus, nick);
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
            while (true) {
                String messageStr = in.readUTF();
                RequestMessage message = RequestMessage.createMessage(messageStr);
                if (message == null) {
                    //TODO: Добавть логирование
                    continue;
                }
                switch (message.getType()) {
                    case END:
                        out.writeUTF(RequestType.END.getValue());
                        return;
                    case SEND_TO_ALL:
                        server.sendToAll(message, nick);
                        break;
                    case SEND_TO_ONE:
                        server.sendToOneCustomer(message,nick);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                completionOfWorkWithTheClient();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Client is exit");
    }

    /**
     * Метод формирует сообщение для клиентов о том, что пользователь отключился и отправляет его
     * также метод удаляет пользователя из списка
     */
    private void completionOfWorkWithTheClient() throws IOException {
        sendMessageAboutChangingCustomerStatus("Пользователь отключился!");
        server.unsubscribe(nick);
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
