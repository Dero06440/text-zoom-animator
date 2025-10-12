package com.zoom;

import com.zoom.controller.MainController;
import com.zoom.view.MainView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main application class
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create view
        MainView view = new MainView(primaryStage);

        // Create controller
        MainController controller = new MainController(view);

        // Show the view
        view.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
