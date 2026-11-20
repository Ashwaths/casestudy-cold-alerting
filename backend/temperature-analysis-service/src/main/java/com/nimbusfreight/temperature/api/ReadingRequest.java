package com.nimbusfreight.temperature.api;
import jakarta.validation.constraints.*;
import java.time.Instant;
public record ReadingRequest(
    @NotBlank @Size(max=64) String truckId,
    @NotNull @DecimalMin("-100.0") @DecimalMax("100.0") Double temperature,
    @NotNull Instant timestamp
) {}
