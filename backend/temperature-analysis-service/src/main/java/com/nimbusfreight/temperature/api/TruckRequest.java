package com.nimbusfreight.temperature.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TruckRequest(
    @NotBlank @Size(max = 64) String truckId,
    @NotBlank @Size(max = 64) String registrationNumber,
    @NotBlank @Size(max = 160) String customerName
) {}
