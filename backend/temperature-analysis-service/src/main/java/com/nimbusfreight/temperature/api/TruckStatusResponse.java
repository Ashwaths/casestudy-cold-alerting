package com.nimbusfreight.temperature.api;

import java.time.Instant;

public record TruckStatusResponse(
    String truckId,
    String registrationNumber,
    String customerName,
    String truckStatus,
    Double latestTemperature,
    Instant latestReadingAt,
    String telemetryState,
    String incidentStatus
) {}
