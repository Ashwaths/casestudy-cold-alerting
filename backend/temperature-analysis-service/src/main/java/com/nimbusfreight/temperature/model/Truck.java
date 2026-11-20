package com.nimbusfreight.temperature.model;

import jakarta.persistence.*;

@Entity
@Table(name = "truck")
public class Truck {
    @Id
    @Column(name = "truck_id", length = 64)
    private String truckId;
    @Column(name = "registration_number", nullable = false, unique = true, length = 64)
    private String registrationNumber;
    @Column(nullable = false, length = 24)
    private String status = "ACTIVE";
    @Column(name = "customer_name", nullable = false, length = 160)
    private String customerName;

    protected Truck() {}
    public Truck(String truckId, String registrationNumber, String customerName) {
        this.truckId = truckId; this.registrationNumber = registrationNumber; this.customerName = customerName;
    }
    public String getTruckId() { return truckId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getStatus() { return status; }
    public String getCustomerName() { return customerName; }
    public void setStatus(String status) { this.status = status; }
    public void updateDetails(String registrationNumber, String customerName) {
        this.registrationNumber = registrationNumber;
        this.customerName = customerName;
    }
}
