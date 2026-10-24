package com.mycompany.techcare;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginWindow extends JFrame {

    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final StaffUserDAO staffUserDAO = new StaffUserDAO();

    public LoginWindow() {
        setTitle("TechCare | Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        content.setPreferredSize(new Dimension(400, 300));

        JLabel title = new JLabel("Welcome to TechCare", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel subtitle = new JLabel("Sign in with your staff account", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 6));
        heading.add(title);
        heading.add(subtitle);

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("Username"));
        form.add(usernameField);
        form.add(new JLabel("Password"));
        form.add(passwordField);

        JButton loginButton = new JButton("Log in");
        loginButton.addActionListener(event -> logIn());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(loginButton, BorderLayout.CENTER);

        content.add(heading, BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);

        setContentPane(content);
        getRootPane().setDefaultButton(loginButton);
        pack();
        setLocationRelativeTo(null);
    }

    private void logIn() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter both your username and password."
            );
            Arrays.fill(password, '\0');
            return;
        }

        try {
            StaffUser user = staffUserDAO.authenticate(username, password);

            if (user == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid username or password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
                passwordField.setText("");
                return;
            }

            AppSession.setCurrentUser(user);

            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
            dispose();

        } catch (SQLException | GeneralSecurityException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not log in: " + exception.getMessage(),
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}