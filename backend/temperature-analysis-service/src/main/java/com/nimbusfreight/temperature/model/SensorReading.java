package com.nimbusfreight.temperature.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sensor_reading",
       indexes = {@Index(name = "idx_reading_truck_event", columnList = "truck_id,event_time"),
                  @Index(name = "idx_reading_received", columnList = "received_time")})
public class SensorReading {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "truck_id", nullable = false)
    private Truck truck;
    @Column(name = "event_time", nullable = false)
    private Instant timestamp;
    @Column(nullable = false)
    private double temperature;
    @Column(name = "received_time", nullable = false)
    private Instant receivedTime;
    @Column(name = "event_key", nullable = false, unique = true, length = 160)
    private String eventKey;

    protected SensorReading() {}
    public SensorReading(Truck truck, Instant timestamp, double temperature, Instant receivedTime, String eventKey) {
        this.truck = truck; this.timestamp = timestamp; this.temperature = temperature;
        this.receivedTime = receivedTime; this.eventKey = eventKey;
    }
    public Long getId() { return id; }
    public Truck getTruck() { return truck; }
    public Instant getTimestamp() { return timestamp; }
    public double getTemperature() { return temperature; }
    public Instant getReceivedTime() { return receivedTime; }
    public String getEventKey() { return eventKey; }
}
