package at.htl.mobilitygamesadvisor.util;

import at.htl.mobilitygamesadvisor.model.User;

public class UserSession {
    private static UserSession instance;
    private User currentUser;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) instance = new UserSession();
        return instance;
    }

    public void login(User user)  { this.currentUser = user; }
    public void logout()          { this.currentUser = null; }
    public User getUser()         { return currentUser; }
    public boolean isLoggedIn()   { return currentUser != null; }
}