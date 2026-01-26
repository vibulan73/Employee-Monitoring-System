package com.monitoring.agent.ui;

import com.monitoring.agent.MonitoringAgent;
import com.monitoring.agent.client.BackendClient;
import com.monitoring.agent.model.AuthResponse;
import com.monitoring.agent.model.SignupRequest;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignupUI {
    private static final Logger logger = LoggerFactory.getLogger(SignupUI.class);

    private final MonitoringAgent agent;
    private final BackendClient backendClient;
    private final Stage stage;

    private TextField firstNameField;
    private TextField lastNameField;
    private TextField userIdField;
    private PasswordField passwordField;
    private TextField jobRoleField;
    private TextField phoneField;
    private Label errorLabel;
    private Button signupButton;
    private Hyperlink loginLink;

    public SignupUI(MonitoringAgent agent, BackendClient backendClient, Stage stage) {
        this.agent = agent;
        this.backendClient = backendClient;
        this.stage = stage;
    }

    public void show() {
        // Title
        Label titleLabel = new Label("Employee Activity Monitor");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        Label subtitleLabel = new Label("Create Your Account");
        subtitleLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
        subtitleLabel.setTextFill(Color.web("#7f8c8d"));

        // Form fields using GridPane for better layout
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setAlignment(Pos.CENTER);
        formGrid.setMaxWidth(400);

        // First Name
        Label firstNameLabel = new Label("First Name:");
        firstNameLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        firstNameField = new TextField();
        firstNameField.setPromptText("Enter first name");
        firstNameField.setStyle("-fx-padding: 8; -fx-font-size: 13;");

        // Last Name
        Label lastNameLabel = new Label("Last Name:");
        lastNameLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        lastNameField = new TextField();
        lastNameField.setPromptText("Enter last name");
        lastNameField.setStyle("-fx-padding: 8; -fx-font-size: 13;");

        // User ID
        Label userIdLabel = new Label("User ID:");
        userIdLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        userIdField = new TextField();
        userIdField.setPromptText("Choose a unique user ID");
        userIdField.setStyle("-fx-padding: 8; -fx-font-size: 13;");

        // Password
        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter password (min 6 chars)");
        passwordField.setStyle("-fx-padding: 8; -fx-font-size: 13;");

        // Job Role
        Label jobRoleLabel = new Label("Job Role:");
        jobRoleLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        jobRoleField = new TextField();
        jobRoleField.setPromptText("Enter job role");
        jobRoleField.setStyle("-fx-padding: 8; -fx-font-size: 13;");

        // Phone Number
        Label phoneLabel = new Label("Phone Number:");
        phoneLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        phoneField = new TextField();
        phoneField.setPromptText("10-digit phone number");
        phoneField.setStyle("-fx-padding: 8; -fx-font-size: 13;");

        // Add fields to grid
        formGrid.add(firstNameLabel, 0, 0);
        formGrid.add(firstNameField, 1, 0);
        formGrid.add(lastNameLabel, 0, 1);
        formGrid.add(lastNameField, 1, 1);
        formGrid.add(userIdLabel, 0, 2);
        formGrid.add(userIdField, 1, 2);
        formGrid.add(passwordLabel, 0, 3);
        formGrid.add(passwordField, 1, 3);
        formGrid.add(jobRoleLabel, 0, 4);
        formGrid.add(jobRoleField, 1, 4);
        formGrid.add(phoneLabel, 0, 5);
        formGrid.add(phoneField, 1, 5);

        // Error label
        errorLabel = new Label("");
        errorLabel.setFont(Font.font("System", 12));
        errorLabel.setTextFill(Color.web("#e74c3c"));
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(400);

        // Signup button
        signupButton = new Button("Sign Up");
        signupButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        signupButton.setPrefWidth(200);
        signupButton.setPrefHeight(45);
        signupButton.setStyle(
                "-fx-background-color: #27ae60; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;");
        signupButton.setOnAction(e -> handleSignup());

        // Hover effects
        signupButton.setOnMouseEntered(e -> signupButton.setStyle(
                "-fx-background-color: #229954; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"));
        signupButton.setOnMouseExited(e -> signupButton.setStyle(
                "-fx-background-color: #27ae60; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"));

        // Login link
        loginLink = new Hyperlink("Already have an account? Login");
        loginLink.setFont(Font.font("System", 12));
        loginLink.setOnAction(e -> showLoginScreen());

        // Layout
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #ecf0f1;");
        layout.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                formGrid,
                errorLabel,
                signupButton,
                loginLink);

        Scene scene = new Scene(layout, 550, 650);
        stage.setScene(scene);
        stage.setTitle("Sign Up - Employee Activity Monitor");
        stage.show();
    }

    private void handleSignup() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String userId = userIdField.getText().trim();
        String password = passwordField.getText();
        String jobRole = jobRoleField.getText().trim();
        String phone = phoneField.getText().trim();

        // Validation
        if (firstName.isEmpty() || lastName.isEmpty() || userId.isEmpty() ||
                password.isEmpty() || jobRole.isEmpty() || phone.isEmpty()) {
            showError("All fields are required");
            return;
        }

        if (userId.length() < 3) {
            showError("User ID must be at least 3 characters long");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters long");
            return;
        }

        if (!phone.matches("^[0-9]{10}$")) {
            showError("Phone number must be exactly 10 digits");
            return;
        }

        // Disable button during signup
        signupButton.setDisable(true);
        signupButton.setText("Creating account...");

        // Perform signup in background thread
        new Thread(() -> {
            try {
                SignupRequest request = new SignupRequest(
                        userId, password, firstName, lastName, jobRole, phone);
                AuthResponse response = backendClient.signup(request);

                Platform.runLater(() -> {
                    logger.info("Signup successful for user: {}", userId);
                    agent.onLoginSuccess(response);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    logger.error("Signup failed", e);
                    String errorMsg = e.getMessage();
                    if (errorMsg.contains("already exists")) {
                        showError("User ID already exists. Please choose a different one.");
                    } else {
                        showError("Signup failed: " + errorMsg);
                    }
                    signupButton.setDisable(false);
                    signupButton.setText("Sign Up");
                });
            }
        }).start();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showLoginScreen() {
        LoginUI loginUI = new LoginUI(agent, backendClient, stage);
        loginUI.show();
    }
}
