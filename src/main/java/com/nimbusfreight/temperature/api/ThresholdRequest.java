package com.nimbusfreight.temperature.api;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
public record ThresholdRequest(@NotNull @DecimalMin("-40.0") @DecimalMax("40.0") Double thresholdCelsius) {}
