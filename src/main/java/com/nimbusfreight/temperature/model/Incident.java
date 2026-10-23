package com.nimbusfreight.temperature.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "incident", indexes = @Index(name = "idx_incident_status", columnList = "status"))
public class Incident {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "incident_id")
    private Long incidentId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "truck_id", nullable = false)
    private Truck truck;
    @Column(name = "start_time", nullable = false)
    private Instant startTime;
    @Column(name = "end_time")
    private Instant endTime;
    @Column(name = "peak_temperature", nullable = false)
    private double peakTemperature;
    @Column(nullable = false, length = 24)
    private String status;
    protected Incident() {}
    public Incident(Truck truck, Instant startTime, double peakTemperature, String status) {
        this.truck = truck; this.startTime = startTime; this.peakTemperature = peakTemperature; this.status = status;
    }
    public void updatePeak(double value) { peakTemperature = Math.max(peakTemperature, value); }
    public void resolve(Instant at) { status = "RESOLVED"; endTime = at; }
    public Long getIncidentId() { return incidentId; }
    public Truck getTruck() { return truck; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public double getPeakTemperature() { return peakTemperature; }
    public String getStatus() { return status; }
}
