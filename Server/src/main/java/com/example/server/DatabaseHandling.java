package com.example.server;

import com.example.api.MessageBox;
import com.example.api.MessageType;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public abstract class DatabaseHandling {
    private final static ReadWriteLock lock = new ReentrantReadWriteLock();

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/auth?user=root&password=Cotton68");
    }

    public static void registrationUsers(String login, String password) {
        lock.writeLock().lock();
        try (Connection connection = getConnection()) {
            log.info("Установлено соединение с базой данных");
            String passwordHashCode = Integer.toString(password.hashCode());
            Statement statement = connection.createStatement();
            String authData = String.format("insert into clients (login,password,nick) values ('%s','%s','%s')",
                    login, passwordHashCode, login);
            statement.executeUpdate(authData);
        } catch (SQLException e) {
            log.error("Не удалось установить соединение с базой данных");
            throw new RuntimeException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static boolean isClientExistsInDatabase(String login, String password) {
        lock.readLock().lock();
        try (Connection connection = getConnection()) {
            log.info("Установлено соединение с базой данных");
            String passwordHashCode = Integer.toString(password.hashCode());
            Statement selectStatement = connection.createStatement();
            String sqlRequest = String.format("SELECT EXISTS (select * from clients where login = '%s' AND " +
                    "password = '%s')", login, passwordHashCode);
            ResultSet resultSet = selectStatement.executeQuery(sqlRequest);
            resultSet.next();
            return resultSet.getInt(1) != 0;
        } catch (SQLException e) {
            log.error("Не удалось установить соединение с базой данных");
            throw new RuntimeException(e);
        } finally {
            lock.readLock().unlock();
        }
    }

    public static String getNickByLoginAndPassword(String login, String password) {
        lock.readLock().lock();
        try (Connection connection = getConnection()) {
            log.info("Установлено соединение с базой данных");
            String passwordHashCode = Integer.toString(password.hashCode());
            Statement selectStatement = connection.createStatement();
            String sqlRequest = String.format("select nick from clients where login = '%s' AND password = '%s'",
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
        } finally {
            lock.readLock().unlock();
        }
    }

//    public static void main(String[] args) {
//        List<MessageBox> oldMessages = new ArrayList<>();
//        oldMessages.add(new MessageBox(MessageType.OUTGOING_MESSAGE_FOR_ALL, LocalDateTime.now(), "h1"));
//        oldMessages.add(new MessageBox(MessageType.OUTGOING_MESSAGE_FOR_ONE_CUSTOMER, LocalDateTime.now(), "h2"));
//        oldMessages.add(new MessageBox(MessageType.INFORMATION_MESSAGE, LocalDateTime.now(), "h3"));
//        oldMessages.add(new MessageBox(MessageType.INCOMING_MESSAGE, LocalDateTime.now(), "h4"));
//        oldMessages.add(new MessageBox(MessageType.WELCOME_MESSAGE, LocalDateTime.now(), "h5"));
//
//        serializeMessages(oldMessages, "t3");
//        serializeMessages(oldMessages, "t4");
//        System.out.println("t3: "+deserializeMessages("t3"));
//        System.out.println("t4: "+deserializeMessages("t4"));
//    }

    public static ArrayList<MessageBox> deserializeMessagesFromDB(String nick) {
        lock.readLock().lock();
        ArrayList<MessageBox> oldMessages = new ArrayList<>();
        try (Connection connection = getConnection()) {
            Statement selectStatement = connection.createStatement();
            String sql = """ 
                         select typeOfMessage, date,message
                         from history
                                  inner join clients c on history.client_id = c.id
                                  inner join type t on history.type_id = t.id
                                  where nick = '%s';
                         """;
            ResultSet resultSet = selectStatement.executeQuery(String.format(sql, nick));
            while (resultSet.next()) {
                MessageType type = MessageType.valueOf(resultSet.getString(1));
                String message = resultSet.getString(3);
                LocalDateTime dateTime = resultSet.getObject(2, LocalDateTime.class);
                oldMessages.add(new MessageBox(type, dateTime, message));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }
        return oldMessages;
    }


    public static void serializeMessagesToDB(List<MessageBox> oldMessageSession, String nick) {
        lock.writeLock().lock();
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                                                                              insert into history (type_id, date, message, client_id) values (?,?,?,?);
                                                                              """);
            var clientID = getClientIdByNick(connection, nick);
            isNotFound(clientID, String.format("Клиент с ником %s не зарегистрирован", nick));
            preparedStatement.setInt(4, clientID);

            for (MessageBox messageBox : oldMessageSession) {
                var typeId = getTypeIdByType(connection, messageBox.getType());
                isNotFound(typeId, String.format("Тип сообщения %s не содержится в базе", messageBox.getType()));
                var data = messageBox.getDateTime().toString();
                var message = messageBox.getMessage();

                preparedStatement.setInt(1, typeId);
                preparedStatement.setString(2, data);
                preparedStatement.setString(3, message);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void addToDBOfflineMessage(MessageBox offlineMessage, String nick) {
        lock.writeLock().lock();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            var clientID = getClientIdByNick(connection, nick);
            isNotFound(clientID, String.format("Клиент с ником %s не зарегистрирован", nick));
            var typeId = getTypeIdByType(connection, offlineMessage.getType());
            isNotFound(typeId, String.format("Тип сообщения %s не содержится в базе", offlineMessage.getType()));
            var date = offlineMessage.getDateTime().toString();
            var message = offlineMessage.getMessage();
            statement.executeUpdate(String.format("insert into history (type_id, date, message, client_id) values " +
                    "('%s','%s','%s','%s')", typeId, date, message, clientID));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static void isNotFound(int paramToCheck, String errorMessage) {
        if (paramToCheck == -1) {
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private static int getTypeIdByType(Connection connection, MessageType type) throws SQLException {
        Statement selectStatement = connection.createStatement();
        String sql = String.format("select id from type where typeOfMessage = '%s'", type);
        ResultSet resultSet = selectStatement.executeQuery(sql);
        if (resultSet.next()) {
            return resultSet.getInt(1);
        } else {
            return -1;
        }
    }

    private static int getClientIdByNick(Connection connection, String nick) throws SQLException {
        Statement selectStatement = connection.createStatement();
        String sql = String.format("select id from clients where nick = '%s'", nick);
        ResultSet resultSet = selectStatement.executeQuery(sql);
        if (resultSet.next()) {
            return resultSet.getInt(1);
        } else {
            return -1;
        }
    }

}