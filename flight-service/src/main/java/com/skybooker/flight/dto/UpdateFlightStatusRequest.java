package com.skybooker.flight.dto;

import com.skybooker.flight.entity.FlightStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateFlightStatusRequest(@NotNull FlightStatus status) {
}
