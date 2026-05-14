package com.skybooker.flight.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SeatCountUpdateRequest(@NotNull @Min(1) Integer seats) {
}
