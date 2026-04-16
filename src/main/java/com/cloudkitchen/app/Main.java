package com.cloudkitchen.app;

import com.cloudkitchen.service.AppFacade;
import com.cloudkitchen.ui.MainApp;
import java.nio.file.Path;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        AppFacade facade = new ApplicationBootstrap().bootstrap(Path.of("data", "cloud_kitchen.db"));
        MainApp.setAppFacade(facade);
        Application.launch(MainApp.class, args);
    }
}
