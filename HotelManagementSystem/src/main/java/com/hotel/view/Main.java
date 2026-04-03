package com.hotel.view;

import com.hotel.util.DatabaseConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Main — Application entry point and primary Stage manager.
 *
 * Bootstraps the JavaFX application, initialises the database,
 * and shows the splash screen as the first scene.
 *
 * @author Grand Vista HMS
 * @version 1.0.0
 */
public class Main extends Application {

    // Shared primary stage — passed to controllers for scene switching
    private static Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Grand Vista Hotel Management System");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(750);
        primaryStage.setResizable(true);

        // Initialise database on start (creates tables + seeds data)
        try {
            DatabaseConnection.getInstance();
        } catch (Exception e) {
            showFatalError("Database Initialisation Failed",
                    "Could not connect to the database.\n" + e.getMessage());
            return;
        }

        // Show splash screen first
        showSplashScreen();

        primaryStage.show();

        // Clean shutdown: close DB on window close
        primaryStage.setOnCloseRequest(event -> {
            DatabaseConnection.getInstance().close();
            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * Displays the hotel landing/splash page.
     */
    public static void showSplashScreen() {
        try {
            SplashController splash = new SplashController();
            Scene scene = splash.createScene();
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showFallbackSplash();
        }
    }

    /**
     * Switches to the main dashboard scene.
     * Called from SplashController when "Book Now" is clicked.
     */
    public static void showDashboard() {
        try {
            DashboardController dashboard = new DashboardController();
            Scene scene = dashboard.createScene();
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
            showFatalError("Navigation Error", "Could not load dashboard: " + e.getMessage());
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    private static void showFatalError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        Platform.exit();
    }

    private static void showFallbackSplash() {
        Label label = new Label("Grand Vista HMS — Loading...");
        VBox root = new VBox(label);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1a1a2e;");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 24px;");
        primaryStage.setScene(new Scene(root, 800, 600));
    }
}
