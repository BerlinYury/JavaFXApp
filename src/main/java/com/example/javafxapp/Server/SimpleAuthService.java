package com.example.javafxapp.Server;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {
    private final List<UserData> users;
    private final int numberOfUsers=10;

    /**
     * Создает новый объект SimpleAuthService, инициализирует список пользователей и заполняет его данными.
     */
    public SimpleAuthService() {
        users = new ArrayList<>();
        for (int i = 0; i < numberOfUsers; i++) {
            users.add(new UserData("l" + i, "p" + i, "nick" + i));
        }
        System.out.println();
    }

    /**
     * Возвращает ник пользователя по его логину и паролю.
     *
     * @param login    логин пользователя
     * @param password пароль пользователя
     * @return ник пользователя, если соответствующий пользователь найден в списке, null в противном случае.
     */
    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        for (UserData user : users) {
            if (user.login.equals(login) && user.password.equals(password)) {
                return user.nick;
            }
        }
        return null;
    }

    private static class UserData {
        private final String login;
        private final String password;
        private final String nick;

        public UserData(String login, String password, String nick) {
            this.login = login;
            this.password = password;
            this.nick = nick;
        }
    }
}
