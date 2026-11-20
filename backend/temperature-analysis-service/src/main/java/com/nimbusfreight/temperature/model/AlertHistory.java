package com.nimbusfreight.temperature.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "alert_history", indexes = @Index(name = "idx_alert_time", columnList = "alert_time"))
public class AlertHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long alertId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;
    @Column(name = "alert_time", nullable = false)
    private Instant alertTime;
    @Column(name = "alert_type", nullable = false, length = 32)
    private String alertType;
    protected AlertHistory() {}
    public AlertHistory(Incident incident, Instant alertTime, String alertType) {
        this.incident = incident; this.alertTime = alertTime; this.alertType = alertType;
    }
    public Long getAlertId() { return alertId; }
    public Incident getIncident() { return incident; }
    public Instant getAlertTime() { return alertTime; }
    public String getAlertType() { return alertType; }
}
