package com.example.server;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public abstract class SimpleAuthService {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/auth?user=root&password=Cotton68");
    }

    public static String getNickFromLoginAndPassword(String login, String password) {
        try (Connection connection = getConnection()) {
            log.info("Установлено соединение с базой данных");
            String passwordHashCode = Integer.toString(password.hashCode());
            Statement selectStatement = connection.createStatement();
            String sqlRequest = String.format("select nick from authenticate where login = '%s' AND password = '%s'",
                    login, passwordHashCode);
            ResultSet resultSet = selectStatement.executeQuery(sqlRequest);
            if (resultSet.next()) {
                return resultSet.getString(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            log.error("Не удалось установить соединение с базой данных");
            throw new RuntimeException(e);
        }
    }

}
