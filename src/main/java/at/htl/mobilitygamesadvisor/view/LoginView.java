package at.htl.mobilitygamesadvisor.view;

public interface LoginView {
    String getUsername();
    String getPassword();
    void showError(String message);
    void navigateToHome();
    void navigateToSignup();
}