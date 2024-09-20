package com.example.server;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public abstract class DatabaseHandling {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/auth?user=root&password=Cotton68");
    }


    public static void registrationUsers(String login, String password) {
        try (Connection connection = getConnection()) {
            log.info("Установлено соединение с базой данных");
            String passwordHashCode = Integer.toString(password.hashCode());
            Statement statement = connection.createStatement();
            String authData = String.format("insert into authenticate (login,password,nick) values ('%s','%s','%s')",
                    login, passwordHashCode, login);
            statement.executeUpdate(authData);
        } catch (SQLException e) {
            log.error("Не удалось установить соединение с базой данных");
            throw new RuntimeException(e);
        }
    }


    public static boolean isClientExistsInDatabase(String login, String password) {
        try (Connection connection = getConnection()) {
            log.info("Установлено соединение с базой данных");
            String passwordHashCode = Integer.toString(password.hashCode());
            Statement selectStatement = connection.createStatement();
            String sqlRequest = String.format("SELECT EXISTS (select * from authenticate where login = '%s' AND " +
                    "password = '%s')", login, passwordHashCode);
            ResultSet resultSet = selectStatement.executeQuery(sqlRequest);
            resultSet.next();
            return resultSet.getInt(1) != 0;
        } catch (SQLException e) {
            log.error("Не удалось установить соединение с базой данных");
            throw new RuntimeException(e);
        }
    }
}