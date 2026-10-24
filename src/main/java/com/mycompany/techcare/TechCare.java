package com.mycompany.techcare;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.SwingUtilities;

public class TechCare {

    public static void main(String[] args) {
        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);
        });
    }
}