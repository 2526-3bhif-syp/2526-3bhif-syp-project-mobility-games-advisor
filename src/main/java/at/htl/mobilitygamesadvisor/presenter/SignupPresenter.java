package at.htl.mobilitygamesadvisor.presenter;

import at.htl.mobilitygamesadvisor.model.UserRepository;
import at.htl.mobilitygamesadvisor.util.PasswordUtil;
import at.htl.mobilitygamesadvisor.view.SignupView;

public class SignupPresenter {

    private final SignupView     view;
    private final UserRepository repo;

    public SignupPresenter(SignupView view) {
        this.view = view;
        this.repo = new UserRepository();
    }

    public void onSignup() {
        String username = view.getUsername().trim();
        String email    = view.getEmail().trim();
        String password = view.getPassword();
        String confirm  = view.getConfirmPassword();

        // —— Validation ————————————————————————————————————————————————————
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            view.showError("Bitte alle Felder ausfüllen.");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            view.showError("Bitte eine gültige E-Mail-Adresse eingeben.");
            return;
        }
        if (!password.equals(confirm)) {
            view.showError("Passwörter stimmen nicht überein.");
            return;
        }
        if (password.length() < 8) {
            view.showError("Passwort muss mindestens 8 Zeichen lang sein.");
            return;
        }

        // —— Persist ———————————————————————————————————————————————————————
        String hash    = PasswordUtil.hash(password);
        boolean created = repo.createUser(username, email, hash);

        if (created) {
            view.showSuccess("✔ Konto erstellt! Du wirst weitergeleitet...");
            // small delay so the user can see the success message
            new Thread(() -> {
                try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(view::navigateToLogin);
            }).start();
        } else {
            view.showError("Benutzername oder E-Mail bereits vergeben.");
        }
    }

    public void onGoToLogin() {
        view.navigateToLogin();
    }
}