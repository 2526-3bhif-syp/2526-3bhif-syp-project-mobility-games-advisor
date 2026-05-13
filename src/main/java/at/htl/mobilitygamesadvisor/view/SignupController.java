package at.htl.mobilitygamesadvisor.view;

import at.htl.mobilitygamesadvisor.presenter.SignupPresenter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class SignupController implements SignupView {

    @FXML private TextField     usernameField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label         errorLabel;

    private SignupPresenter presenter;

    @FXML
    public void initialize() {
        presenter = new SignupPresenter(this);
    }

    // —— FXML handlers ————————————————————————————————————————————————————————

    @FXML
    private void handleSignup() {
        presenter.onSignup();
    }

    @FXML
    private void handleGoToLogin() {
        presenter.onGoToLogin();
    }

    // —— SignupView implementation ————————————————————————————————————————————

    @Override
    public String getUsername() {
        return usernameField.getText();
    }

    @Override
    public String getEmail() {
        return emailField.getText();
    }

    @Override
    public String getPassword() {
        return passwordField.getText();
    }

    @Override
    public String getConfirmPassword() {
        return confirmPasswordField.getText();
    }

    @Override
    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle(
                "-fx-text-fill: #c0392b;" +
                        "-fx-background-color: #fdf0ef;" +
                        "-fx-border-color: #f5c6c2;" +
                        "-fx-border-radius: 6px;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-padding: 8 12 8 12;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-family: 'Segoe UI';"
        );
        errorLabel.setVisible(true);
    }

    @Override
    public void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle(
                "-fx-text-fill: #2d7a5c;" +
                        "-fx-background-color: #e8f4ef;" +
                        "-fx-border-color: #5cad8a;" +
                        "-fx-border-radius: 6px;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-padding: 8 12 8 12;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-family: 'Segoe UI';"
        );
        errorLabel.setVisible(true);
    }


    @Override
    public void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/at/htl/mobilitygamesadvisor/login.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}