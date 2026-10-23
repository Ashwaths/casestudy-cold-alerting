CREATE DATABASE IF NOT EXISTS nimbus_freight;
USE nimbus_freight;

CREATE TABLE truck (
  truck_id VARCHAR(64) PRIMARY KEY,
  registration_number VARCHAR(64) NOT NULL UNIQUE,
  status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
  customer_name VARCHAR(160) NOT NULL
);

CREATE TABLE sensor_reading (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  truck_id VARCHAR(64) NOT NULL,
  event_time TIMESTAMP(6) NOT NULL,
  temperature DOUBLE NOT NULL,
  received_time TIMESTAMP(6) NOT NULL,
  event_key VARCHAR(160) NOT NULL UNIQUE,
  CONSTRAINT fk_reading_truck FOREIGN KEY (truck_id) REFERENCES truck(truck_id),
  INDEX idx_reading_truck_event (truck_id, event_time),
  INDEX idx_reading_received (received_time)
);

CREATE TABLE incident (
  incident_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  truck_id VARCHAR(64) NOT NULL,
  start_time TIMESTAMP(6) NOT NULL,
  end_time TIMESTAMP(6) NULL,
  peak_temperature DOUBLE NOT NULL,
  status VARCHAR(24) NOT NULL,
  CONSTRAINT fk_incident_truck FOREIGN KEY (truck_id) REFERENCES truck(truck_id),
  INDEX idx_incident_status (status),
  INDEX idx_incident_truck_start (truck_id, start_time)
);

CREATE TABLE alert_history (
  alert_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  incident_id BIGINT NOT NULL,
  alert_time TIMESTAMP(6) NOT NULL,
  alert_type VARCHAR(32) NOT NULL,
  CONSTRAINT fk_alert_incident FOREIGN KEY (incident_id) REFERENCES incident(incident_id),
  INDEX idx_alert_time (alert_time)
);

CREATE TABLE configuration (
  config_key VARCHAR(80) PRIMARY KEY,
  config_value VARCHAR(200) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL
);

INSERT INTO truck(truck_id, registration_number, status, customer_name)
VALUES ('TRUCK-101', 'KA-01-NB-0101', 'ACTIVE', 'Nimbus Demo Customer')
ON DUPLICATE KEY UPDATE truck_id=VALUES(truck_id);
