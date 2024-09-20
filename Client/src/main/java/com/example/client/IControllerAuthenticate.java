package com.example.client;

public interface IControllerAuthenticate {
    void onSuccess();
    void onError();
    void onBusy();
    void closeAllWindows();
    void restartTimer();
    void offTimer();
}
