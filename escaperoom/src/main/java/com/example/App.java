package com.example;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize singleton scene manager
        SceneManager.init(primaryStage);

        // Show the home screen (your home.fxml). If you want to start at a different place, change here.
        SceneManager.getInstance().showHome();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


