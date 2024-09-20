package com.example.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;

public class UIServer extends Application  implements IUIServer{
    @Getter
    private Stage stage;

    public static void startFXWindow(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage=stage;
        FXMLLoader loader = new FXMLLoader(UIServer.class.getResource("server-view.fxml"));
        Parent root = loader.load();
        ControllerServer controller = loader.getController();
        controller.setUIServer(this);

        Scene scene = new Scene(root, 200, 150);
        stage.setScene(scene);
        stage.show();
    }
}
