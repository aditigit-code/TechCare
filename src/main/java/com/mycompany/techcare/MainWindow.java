package com.mycompany.techcare;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class MainWindow extends javax.swing.JFrame {

    private final Color sidebarColor = new Color(25, 36, 55);
    private final Color selectedColor = new Color(49, 68, 96);
    private final Color pageColor = new Color(245, 247, 250);
    private final Color textColor = new Color(35, 45, 60);

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pageCards = new JPanel(cardLayout);
    private final JLabel pageTitle = new JLabel("Dashboard");
    private final List<JButton> navigationButtons = new ArrayList<>();

    private DashboardPage dashboardPage;
    private CustomerPage customerPage;
    private DevicePage devicePage;
    private TechnicianPage technicianPage;
    private ServiceTicketPage serviceTicketPage;
    private InventoryPage inventoryPage;
    private BillingPage billingPage;
    private ReportsPage reportsPage;

    public MainWindow() {
        setTitle("TechCare | Computer Service Center");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        setSize(1180, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createMainArea(), BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(225, 0));
        sidebar.setBackground(sidebarColor);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(24, 14, 20, 14));

        JLabel brand = new JLabel("TECHCARE");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("SansSerif", Font.BOLD, 22));
        brand.setBorder(new EmptyBorder(4, 10, 2, 0));

        JLabel subtitle = new JLabel("COMPUTER SERVICE CENTER");
        subtitle.setForeground(new Color(180, 193, 211));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
        subtitle.setBorder(new EmptyBorder(0, 11, 28, 0));

        sidebar.add(brand);
        sidebar.add(subtitle);

        JLabel section = new JLabel("WORKSPACE");
        section.setForeground(new Color(160, 174, 195));
        section.setFont(new Font("SansSerif", Font.BOLD, 10));
        section.setBorder(new EmptyBorder(0, 11, 10, 0));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(section);

        addNavigationItem(sidebar, "Dashboard");
        addNavigationItem(sidebar, "Customers");
        addNavigationItem(sidebar, "Devices");
        addNavigationItem(sidebar, "Technicians");
        addNavigationItem(sidebar, "Service Tickets");
        addNavigationItem(sidebar, "Inventory");
        addNavigationItem(sidebar, "Billing");
        addNavigationItem(sidebar, "Reports");

        return sidebar;
    }

    private void addNavigationItem(JPanel sidebar, String pageName) {
        sidebar.add(createNavigationButton(pageName, pageName));
        sidebar.add(Box.createVerticalStrut(5));
    }

    private JButton createNavigationButton(String label, String pageName) {
        JButton button = new JButton(label);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(sidebarColor);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMargin(new Insets(0, 14, 0, 0));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        navigationButtons.add(button);

        button.addActionListener(event -> {
            cardLayout.show(pageCards, pageName);
            pageTitle.setText(pageName);

            for (JButton navButton : navigationButtons) {
                navButton.setBackground(sidebarColor);
            }
            button.setBackground(selectedColor);

            if ("Dashboard".equals(pageName) && dashboardPage != null) {
                dashboardPage.refreshDashboard();
            } else if ("Customers".equals(pageName)
                    && customerPage != null) {
                customerPage.loadCustomers();
            } else if ("Devices".equals(pageName)
                    && devicePage != null) {
                devicePage.loadDevices();
            } else if ("Technicians".equals(pageName)
                    && technicianPage != null) {
                technicianPage.loadTechnicians();
            } else if ("Reports".equals(pageName)
                    && reportsPage != null) {
                reportsPage.refreshReports();
            }
        });

        if ("Dashboard".equals(pageName)) {
            button.setBackground(selectedColor);
        }

        return button;
    }

    private JPanel createMainArea() {
        JPanel mainArea = new JPanel(new BorderLayout(0, 24));
        mainArea.setBackground(pageColor);
        mainArea.setBorder(new EmptyBorder(28, 32, 30, 32));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        pageTitle.setForeground(textColor);
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 26));

        header.add(pageTitle, BorderLayout.WEST);

        pageCards.setOpaque(false);

        dashboardPage = new DashboardPage();
        pageCards.add(dashboardPage, "Dashboard");

        customerPage = new CustomerPage();
        pageCards.add(customerPage, "Customers");

        devicePage = new DevicePage();
        pageCards.add(devicePage, "Devices");

        technicianPage = new TechnicianPage();
        pageCards.add(technicianPage, "Technicians");

        serviceTicketPage = new ServiceTicketPage();
        pageCards.add(serviceTicketPage, "Service Tickets");

        inventoryPage = new InventoryPage();
        pageCards.add(inventoryPage, "Inventory");

        billingPage = new BillingPage();
        pageCards.add(billingPage, "Billing");

        reportsPage = new ReportsPage();
        pageCards.add(reportsPage, "Reports");

        mainArea.add(header, BorderLayout.NORTH);
        mainArea.add(pageCards, BorderLayout.CENTER);

        return mainArea;
    }
}