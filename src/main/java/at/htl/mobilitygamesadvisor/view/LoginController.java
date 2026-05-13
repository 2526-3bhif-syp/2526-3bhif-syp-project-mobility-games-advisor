package at.htl.mobilitygamesadvisor.view;
import javafx.fxml.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import at.htl.mobilitygamesadvisor.presenter.*;

import java.io.IOException;

public class LoginController implements LoginView {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private LoginPresenter presenter;

    @FXML
    public void initialize() {
        presenter = new LoginPresenter(this);
    }

    @FXML
    private void handleLogin() { presenter.onLogin(); }

    @FXML
    private void handleGoToSignup() { presenter.onGoToSignup(); }

    @Override public String getUsername() { return usernameField.getText(); }
    @Override public String getPassword() { return passwordField.getText(); }

    @Override
    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @Override
    public void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/at/htl/mobilitygamesadvisor/main-view.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void navigateToSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/at/htl/mobilitygamesadvisor/signup.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) { e.printStackTrace(); }
    }
}