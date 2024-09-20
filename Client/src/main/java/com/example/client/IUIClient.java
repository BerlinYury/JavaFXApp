package com.example.client;

import javafx.stage.Stage;

public interface IUIClient {
   Stage getStartStage();
   Stage getAuthenticateStage();
   Stage getRegistrationStage();
   ControllerClient getControllerClient();
   ControllerAuthenticate getControllerAuthenticate();
   ControllerRegistration getControllerRegistration();
}
