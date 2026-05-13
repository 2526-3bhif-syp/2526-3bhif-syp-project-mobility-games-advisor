package at.htl.mobilitygamesadvisor.presenter;
import at.htl.mobilitygamesadvisor.model.User;
import at.htl.mobilitygamesadvisor.model.UserRepository;
import at.htl.mobilitygamesadvisor.util.UserSession;
import at.htl.mobilitygamesadvisor.view.LoginView;

import at.htl.mobilitygamesadvisor.util.PasswordUtil;

public class LoginPresenter {
    private final LoginView view;
    private final UserRepository repo;

    public LoginPresenter(LoginView view) {
        this.view = view;
        this.repo = new UserRepository();
    }

    public void onLogin() {

        String username = view.getUsername().trim();
        String password = view.getPassword();



        if (username.isEmpty() || password.isEmpty()) {
            view.showError("Please fill in all fields.");
            return;
        }

        User user = repo.findByUsername(username);
        if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
            view.showError("Invalid username or password.");
            return;
        }
        UserSession.getInstance().login(user);

        repo.updateLastLogin(user.getId());
        view.navigateToHome();
    }

    public void onGoToSignup() {
        view.navigateToSignup();
    }
}