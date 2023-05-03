package com.example.client;

import com.example.api.RequestType;
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
    private String nick = "";

    /**
     * Создает новый экземпляр класса ChatClient с указанным контроллером.
     *
     * @param controller экземпляр контроллера, связанный с данным клиентом
     */
    public ChatClient(ControllerClient controller) {
        this.controller = controller;
    }

    /**
     * Метод для установления соединения с сервером. Создает новый сокет и потоки ввода-вывода.
     * В новом потоке запускает бесконечный цикл чтения сообщений от сервера.
     * Если сообщение начинается с констант, содержащих информацию об аутентификации, то обрабатывает их через соответствующие методы
     * контроллера аутентификации.
     * Иначе добавляет входящее сообщение в контроллер для отображения в чате.
     * При возникновении ошибок или завершении цикла закрывает соединение и потоки ввода-вывода.
     * Поток запускается в демоническом режиме.
     */
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
                        System.out.println(messageStr);
                        //   /response nick4 hi bro
                        ResponseMessage message = ResponseMessage.createMessage(messageStr);
                        if (message == null) {
                            //TODO: Добавть логирование
                            continue;
                        }
                        switch (message.getType()) {
                            case END:
                                out.writeUTF(RequestType.END.getValue());
                                return;
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
                                controller.addButton(message.getClientsChangeList());
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

    /**
     * Отправляет сообщение на сервер.
     *
     * @param msg сообщение для отправки
     */
    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Закрывает соединение с сервером.
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

    /**
     * Связывает контроллер аутентификации с данным клиентом.
     *
     * @param controllerAuthenticate экземпляр контроллера аутентификации
     */
    public void takeControllerAuthenticate(ControllerAuthenticate controllerAuthenticate) {
        this.controllerAuthenticate = controllerAuthenticate;
    }
}
