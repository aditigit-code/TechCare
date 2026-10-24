package com.mycompany.techcare;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class ReportsPage extends JPanel {

    private final ReportsDAO reportsDAO = new ReportsDAO();

    private final JLabel invoiceCountValue = new JLabel("—");
    private final JLabel invoicedValue = new JLabel("—");
    private final JLabel receivedValue = new JLabel("—");
    private final JLabel outstandingValue = new JLabel("—");

    private final DefaultTableModel statusModel = new DefaultTableModel(
            new String[]{"Ticket Status", "Ticket Count"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultTableModel stockModel = new DefaultTableModel(
            new String[]{"SKU", "Part", "In Stock", "Reorder Level"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public ReportsPage() {
        setLayout(new BorderLayout(12, 12));
        setOpaque(false);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        add(createHeader(), BorderLayout.NORTH);
        add(createReportTables(), BorderLayout.CENTER);

        refreshReports();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JLabel heading = new JLabel("Reports Overview");
        heading.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 20));
        heading.setForeground(new Color(35, 45, 60));

        JButton refreshButton = new JButton("Refresh Reports");
        refreshButton.addActionListener(event -> refreshReports());

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(heading, BorderLayout.WEST);
        titleRow.add(refreshButton, BorderLayout.EAST);

        JPanel financialCards = new JPanel(new GridLayout(1, 4, 12, 0));
        financialCards.setOpaque(false);
        financialCards.add(createSummaryCard("Invoices", invoiceCountValue));
        financialCards.add(createSummaryCard("Total Invoiced", invoicedValue));
        financialCards.add(createSummaryCard("Payments Received", receivedValue));
        financialCards.add(createSummaryCard("Outstanding", outstandingValue));

        panel.add(titleRow, BorderLayout.NORTH);
        panel.add(financialCards, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSummaryCard(String title, JLabel value) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 230, 237)),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(105, 117, 132));

        value.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 21));
        value.setForeground(new Color(35, 45, 60));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JPanel createReportTables() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setOpaque(false);

        JTable statusTable = new JTable(statusModel);
        JTable stockTable = new JTable(stockModel);

        panel.add(createTablePanel("Tickets by Status", statusTable));
        panel.add(createTablePanel("Parts at or Below Reorder Level", stockTable));

        return panel;
    }

    private JPanel createTablePanel(String title, JTable table) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 230, 237)),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel heading = new JLabel(title);
        heading.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 15));

        panel.add(heading, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    public void refreshReports() {
        statusModel.setRowCount(0);
        stockModel.setRowCount(0);

        try {
            ReportsDAO.FinancialSummary summary =
                    reportsDAO.getFinancialSummary();

            invoiceCountValue.setText(
                    String.valueOf(summary.invoiceCount()));
            invoicedValue.setText(summary.invoiced().toPlainString());
            receivedValue.setText(summary.received().toPlainString());
            outstandingValue.setText(
                    summary.outstanding().toPlainString());

            for (ReportsDAO.StatusCount row
                    : reportsDAO.getTicketStatusCounts()) {
                statusModel.addRow(new Object[]{
                    row.status(), row.count()
                });
            }

            for (ReportsDAO.LowStockPart part
                    : reportsDAO.getLowStockParts()) {
                stockModel.addRow(new Object[]{
                    part.sku(),
                    part.name(),
                    part.quantity(),
                    part.reorderLevel()
                });
            }

        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load reports.\n\n" + exception.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}