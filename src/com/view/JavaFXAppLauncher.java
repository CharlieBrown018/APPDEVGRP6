package com.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class JavaFXAppLauncher extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create your JavaFX UI and show it here
        landingpage ln = new landingpage();
        ln.setVisible(true);
    }

    public static void launchJavaFXApp(String[] args) {
        // Start JavaFX Application Thread
        new Thread(() -> Application.launch(JavaFXAppLauncher.class, args)).start();

        // Add Shutdown Hook to stop JavaFX Application thread
        Runtime.getRuntime().addShutdownHook(new Thread(() -> Platform.exit()));
    }
}