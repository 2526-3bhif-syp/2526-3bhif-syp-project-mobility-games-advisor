package at.htl.mobilitygamesadvisor.view;

public interface SignupView {
    String getUsername();
    String getEmail();
    String getPassword();
    String getConfirmPassword();
    void showError(String message);
    void showSuccess(String message);
    void navigateToLogin();
}