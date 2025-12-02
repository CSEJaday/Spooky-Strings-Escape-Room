package com.example;

import com.model.User;
import com.model.UserList;
import com.model.UserLoader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

/**
 * LoginController wired to login.fxml
 */
public class LoginController {
    @FXML private TextField usernameField;
    @FXML private TextField passwordField;

    @FXML
    public void switchToHome() {
        try { SceneManager.getInstance().showHome(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void switchToDifficulty() {
        String username = usernameField == null ? "" : usernameField.getText().trim();
        String password = passwordField == null ? "" : passwordField.getText();

        if (username.isEmpty()) { showAlert("Login", "Please enter a username."); return; }
        if (password.isEmpty()) { showAlert("Login", "Please enter a password."); return; }

        UserList users = UserList.getInstance();
        if (users == null) {
            showAlert("Login", "User backend unavailable.");
            return;
        }

        User user = users.getUserByName(username);
        if (user == null) {
            showAlert("Login", "User not found. Try signing up.");
            return;
        }

        if (!password.equals(user.getPassword())) {
            showAlert("Login", "Incorrect password.");
            return;
        }

        // Try set current user in backend singletons (defensive)
        setCurrentUserInBackend(user);

        Platform.runLater(() -> {
            try { SceneManager.getInstance().showDifficulty(); } catch (Exception e) { e.printStackTrace(); showAlert("Navigation error", e.getMessage()); }
        });
    }

    private boolean setCurrentUserInBackend(User user) {
        try {
            try {
                UserLoader loader = UserLoader.getInstance();
                if (loader != null) {
                    try { loader.getClass().getMethod("setCurrentUser", User.class).invoke(loader, user); return true; } catch (NoSuchMethodException ignored) {}
                    try { loader.getClass().getMethod("setLoggedInUser", User.class).invoke(loader, user); return true; } catch (NoSuchMethodException ignored) {}
                    try { loader.getClass().getMethod("login", User.class).invoke(loader, user); return true; } catch (NoSuchMethodException ignored) {}
                }
            } catch (Throwable ignored) {}

            try {
                UserList ul = UserList.getInstance();
                if (ul != null) {
                    try { ul.getClass().getMethod("setCurrentUser", User.class).invoke(ul, user); return true; } catch (NoSuchMethodException ignored) {}
                    try { ul.getClass().getMethod("setLoggedInUser", User.class).invoke(ul, user); return true; } catch (NoSuchMethodException ignored) {}
                }
            } catch (Throwable ignored) {}
        } catch (Throwable t) { t.printStackTrace(); }
        return false;
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle(title);
            a.setHeaderText(null);
            a.setContentText(message);
            a.showAndWait();
        });
    }
}


