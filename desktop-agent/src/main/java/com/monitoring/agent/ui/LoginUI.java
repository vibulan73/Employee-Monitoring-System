package com.monitoring.agent.ui;

import com.monitoring.agent.MonitoringAgent;
import com.monitoring.agent.client.BackendClient;
import com.monitoring.agent.model.AuthResponse;
import com.monitoring.agent.model.LoginRequest;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginUI {
    private static final Logger logger = LoggerFactory.getLogger(LoginUI.class);

    private final MonitoringAgent agent;
    private final BackendClient backendClient;
    private final Stage stage;

    private TextField userIdField;
    private PasswordField passwordField;
    private Label errorLabel;
    private Button loginButton;
    private Hyperlink signupLink;

    public LoginUI(MonitoringAgent agent, BackendClient backendClient, Stage stage) {
        this.agent = agent;
        this.backendClient = backendClient;
        this.stage = stage;
    }

    public void show() {
        // Title
        Label titleLabel = new Label("Employee Activity Monitor");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        Label subtitleLabel = new Label("Login to Continue");
        subtitleLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
        subtitleLabel.setTextFill(Color.web("#7f8c8d"));

        // User ID field
        Label userIdLabel = new Label("User ID:");
        userIdLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));

        userIdField = new TextField();
        userIdField.setPromptText("Enter your user ID");
        userIdField.setMaxWidth(300);
        userIdField.setStyle("-fx-padding: 10; -fx-font-size: 14;");

        // Password field
        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));

        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setMaxWidth(300);
        passwordField.setStyle("-fx-padding: 10; -fx-font-size: 14;");

        // Error label
        errorLabel = new Label("");
        errorLabel.setFont(Font.font("System", 12));
        errorLabel.setTextFill(Color.web("#e74c3c"));
        errorLabel.setVisible(false);

        // Login button
        loginButton = new Button("Login");
        loginButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        loginButton.setPrefWidth(200);
        loginButton.setPrefHeight(45);
        loginButton.setStyle(
                "-fx-background-color: #3498db; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;");
        loginButton.setOnAction(e -> handleLogin());

        // Hover effects
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(
                "-fx-background-color: #2980b9; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(
                "-fx-background-color: #3498db; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"));

        // Signup link
        signupLink = new Hyperlink("Don't have an account? Sign up");
        signupLink.setFont(Font.font("System", 12));
        signupLink.setOnAction(e -> showSignupScreen());

        // Press Enter to login
        passwordField.setOnAction(e -> handleLogin());
        userIdField.setOnAction(e -> passwordField.requestFocus());

        // Layout
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #ecf0f1;");
        layout.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                userIdLabel,
                userIdField,
                passwordLabel,
                passwordField,
                errorLabel,
                loginButton,
                signupLink);

        Scene scene = new Scene(layout, 500, 600);
        stage.setScene(scene);
        stage.setTitle("Login - Employee Activity Monitor");
        stage.show();
    }

    private void handleLogin() {
        String userId = userIdField.getText().trim();
        String password = passwordField.getText();

        if (userId.isEmpty() || password.isEmpty()) {
            showError("Please enter both user ID and password");
            return;
        }

        // Disable button during login
        loginButton.setDisable(true);
        loginButton.setText("Logging in...");

        // Perform login in background thread
        new Thread(() -> {
            try {
                LoginRequest request = new LoginRequest(userId, password);
                AuthResponse response = backendClient.login(request);

                Platform.runLater(() -> {
                    logger.info("Login successful for user: {}", userId);
                    agent.onLoginSuccess(response);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    logger.error("Login failed", e);
                    showError("Login failed: Invalid user ID or password");
                    loginButton.setDisable(false);
                    loginButton.setText("Login");
                });
            }
        }).start();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showSignupScreen() {
        SignupUI signupUI = new SignupUI(agent, backendClient, stage);
        signupUI.show();
    }
}

