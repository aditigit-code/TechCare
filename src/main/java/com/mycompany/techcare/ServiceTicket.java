package com.mycompany.techcare;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ServiceTicket {

    private int ticketId;
    private int deviceId;
    private int categoryId;
    private Integer assignedTechnicianId;
    private int createdBy;
    private String status;
    private String priority;
    private String reportedIssue;
    private String intakeCondition;
    private String accessoriesReceived;
    private String intakeNotes;
    private String diagnosis;
    private BigDecimal estimatedCost;
    private String resolution;
    private LocalDate expectedCompletionDate;

    public ServiceTicket() {
    }

    public ServiceTicket(int deviceId, int categoryId, int createdBy,
                         String priority, String reportedIssue) {
        this.deviceId = deviceId;
        this.categoryId = categoryId;
        this.createdBy = createdBy;
        this.priority = priority;
        this.reportedIssue = reportedIssue;
        this.status = "RECEIVED";
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getAssignedTechnicianId() {
        return assignedTechnicianId;
    }

    public void setAssignedTechnicianId(Integer assignedTechnicianId) {
        this.assignedTechnicianId = assignedTechnicianId;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getReportedIssue() {
        return reportedIssue;
    }

    public void setReportedIssue(String reportedIssue) {
        this.reportedIssue = reportedIssue;
    }

    public String getIntakeCondition() {
        return intakeCondition;
    }

    public void setIntakeCondition(String intakeCondition) {
        this.intakeCondition = intakeCondition;
    }

    public String getAccessoriesReceived() {
        return accessoriesReceived;
    }

    public void setAccessoriesReceived(String accessoriesReceived) {
        this.accessoriesReceived = accessoriesReceived;
    }

    public String getIntakeNotes() {
        return intakeNotes;
    }

    public void setIntakeNotes(String intakeNotes) {
        this.intakeNotes = intakeNotes;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public LocalDate getExpectedCompletionDate() {
        return expectedCompletionDate;
    }

    public void setExpectedCompletionDate(LocalDate expectedCompletionDate) {
        this.expectedCompletionDate = expectedCompletionDate;
    }
}