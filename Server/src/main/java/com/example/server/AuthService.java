package com.example.server;

public interface AuthService {
    String getNickByLoginAndPassword(String login, String password);
    String authenticate(String authMsg);
}

