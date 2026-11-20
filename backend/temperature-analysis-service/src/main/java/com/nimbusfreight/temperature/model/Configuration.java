package com.nimbusfreight.temperature.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "configuration")
public class Configuration {
    @Id
    @Column(name = "config_key", length = 80)
    private String key;
    @Column(name = "config_value", nullable = false, length = 200)
    private String value;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    protected Configuration() {}
    public Configuration(String key, String value) { this.key = key; this.value = value; this.updatedAt = Instant.now(); }
    public String getKey() { return key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; this.updatedAt = Instant.now(); }
}
