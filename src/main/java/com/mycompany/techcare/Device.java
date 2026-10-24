package com.mycompany.techcare;

public class Device {

    private int deviceId;
    private int customerId;
    private String customerName;
    private String deviceType;
    private String brand;
    private String model;
    private String serialNumber;
    private String operatingSystem;
    private String notes;

    public Device(int deviceId, int customerId, String customerName,
                  String deviceType, String brand, String model,
                  String serialNumber, String operatingSystem, String notes) {
        this.deviceId = deviceId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.deviceType = deviceType;
        this.brand = brand;
        this.model = model;
        this.serialNumber = serialNumber;
        this.operatingSystem = operatingSystem;
        this.notes = notes;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public String getNotes() {
        return notes;
    }
}