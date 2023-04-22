package com.example.javafxapp.Client;

public enum Constants {
    END("/end"),
    AUTH("/auth"),
    AUTH_CHANGES("/changes"),
    SEND_TO_ONE("/to"),
    SEND_TO_ALL("/toAll"),
    AUTH_OK("/authOK"),
    AUTH_FAILED("/authFailed"),
    AUTH_NICK_BUSY("/authNickBusy");

    private final String value;

    Constants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
