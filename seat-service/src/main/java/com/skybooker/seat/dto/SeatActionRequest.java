package com.skybooker.seat.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SeatActionRequest(@NotNull UUID seatId) {
}
