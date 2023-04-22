package com.example.javafxapp.Server;

public interface AuthService {
    String getNickByLoginAndPassword(String login, String password);
}
