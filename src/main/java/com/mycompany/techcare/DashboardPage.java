package com.mycompany.techcare;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class DashboardPage extends JPanel {

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    private final JLabel openTicketsValue = new JLabel("—");
    private final JLabel inProgressValue = new JLabel("—");
    private final JLabel readyValue = new JLabel("—");
    private final JLabel lowStockValue = new JLabel("—");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{
                "Ticket ID", "Customer", "Device", "Status", "Received"
            }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public DashboardPage() {
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);

        JPanel intro = new JPanel(new BorderLayout());
        intro.setOpaque(false);

        JLabel heading = new JLabel("Welcome to TechCare");
        heading.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 20));
        heading.setForeground(new Color(35, 45, 60));

        JLabel description = new JLabel(
                "Live overview of service work and spare-part stock."
        );
        description.setForeground(new Color(105, 117, 132));

        JPanel introText = new JPanel(new GridLayout(2, 1, 0, 5));
        introText.setOpaque(false);
        introText.add(heading);
        introText.add(description);

        JButton refreshButton = new JButton("Refresh Dashboard");
        refreshButton.addActionListener(event -> refreshDashboard());

        intro.add(introText, BorderLayout.WEST);
        intro.add(refreshButton, BorderLayout.EAST);

        JPanel metrics = new JPanel(new GridLayout(1, 4, 14, 0));
        metrics.setOpaque(false);
        metrics.add(createMetricCard("Open Tickets", openTicketsValue));
        metrics.add(createMetricCard("In Progress", inProgressValue));
        metrics.add(createMetricCard("Ready for Pickup", readyValue));
        metrics.add(createMetricCard("Low Stock Items", lowStockValue));

        top.add(intro, BorderLayout.NORTH);
        top.add(metrics, BorderLayout.CENTER);

        JTable recentTable = new JTable(tableModel);
        JPanel recentPanel = new JPanel(new BorderLayout(0, 10));
        recentPanel.setBackground(Color.WHITE);
        recentPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 230, 237)),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel recentHeading = new JLabel("Recent Service Tickets");
        recentHeading.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 16));
        recentPanel.add(recentHeading, BorderLayout.NORTH);
        recentPanel.add(new JScrollPane(recentTable), BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(recentPanel, BorderLayout.CENTER);

        refreshDashboard();
    }

    private JPanel createMetricCard(String title, JLabel value) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 230, 237)),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(105, 117, 132));

        value.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 28));
        value.setForeground(new Color(35, 45, 60));
        value.setHorizontalAlignment(SwingConstants.LEFT);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    public void refreshDashboard() {
        try {
            DashboardDAO.Summary summary = dashboardDAO.getSummary();

            openTicketsValue.setText(String.valueOf(summary.openTickets()));
            inProgressValue.setText(String.valueOf(summary.inProgress()));
            readyValue.setText(String.valueOf(summary.readyForPickup()));
            lowStockValue.setText(String.valueOf(summary.lowStockItems()));

            tableModel.setRowCount(0);
            for (DashboardDAO.RecentTicket ticket
                    : dashboardDAO.getRecentTickets()) {
                tableModel.addRow(new Object[]{
                    ticket.ticketId(),
                    ticket.customer(),
                    ticket.device(),
                    ticket.status(),
                    ticket.receivedAt()
                });
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load dashboard data.\n\n"
                    + exception.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}