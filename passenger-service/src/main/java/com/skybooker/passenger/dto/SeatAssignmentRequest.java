package com.skybooker.passenger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SeatAssignmentRequest(@NotNull UUID seatId,@NotBlank String seatNumber) {
}
