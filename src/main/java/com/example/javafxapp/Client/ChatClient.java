package com.example.javafxapp.Client;

import javafx.scene.control.Alert;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final ControllerClient controller;
    private final String LOCALHOST = "localhost";
    private final int PORT = 8189;
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
     Метод для установления соединения с сервером. Создает новый сокет и потоки ввода-вывода.
     В новом потоке запускает бесконечный цикл чтения сообщений от сервера.
     Если сообщение начинается с констант, содержащих информацию об аутентификации, то обрабатывает их через соответствующие методы контроллера аутентификации.
     Иначе добавляет входящее сообщение в контроллер для отображения в чате.
     При возникновении ошибок или завершении цикла закрывает соединение и потоки ввода-вывода.
     Поток запускается в демоническом режиме.
     */
    public void openConnection() {
        try {
            socket = new Socket(LOCALHOST, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            Thread threadClient = new Thread(() -> {
                try {
                    while (true) {
                        final String message = in.readUTF();
                        if (Constants.END.getValue().equals(message)) {
                            break;
                        }
                        if (message.startsWith(Constants.AUTH_OK.getValue())) {
                            this.nick = message.split(" ")[1];//Сохраняем ник клиента, для метода addButton в классе ControllerClient
                            controllerAuthenticate.onSuccess();//Метод открывает главное окно, закрывает окно авторизации,
                        } else if (message.startsWith(Constants.AUTH_FAILED.getValue())) {
                            controllerAuthenticate.onError();//Метод повторяет запрос логина и пароля, выводит сообщение об ошибке
                        } else if (message.startsWith(Constants.AUTH_NICK_BUSY.getValue())) {
                            controllerAuthenticate.onBusy();//Метод повторяет запрос логина и пароля, выводит сообщение об ошибке
                        } else {
                            controller.addIncomingMessage(message);
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
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка отправки сообщения");
            alert.setHeaderText(null);
            alert.showAndWait();
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
