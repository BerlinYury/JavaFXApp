package com.example.client;

import jakarta.enterprise.inject.spi.CDI;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.io.InputStream;

public class CdiFXMLLoaderCreator {

    public static FXMLLoader load(Class<? extends Controller> controllerClass) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(param -> CDI.current().select(controllerClass).get());
        return loader;
    }
}
