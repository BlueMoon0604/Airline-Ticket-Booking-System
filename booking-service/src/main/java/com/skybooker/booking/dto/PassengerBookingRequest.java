package com.skybooker.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record PassengerBookingRequest(
        @NotBlank String title,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull LocalDate dateOfBirth,
        @NotBlank String gender,
        @NotBlank String passportNumber,
        @NotBlank String nationality,
        @NotNull LocalDate passportExpiry,
        @NotBlank String passengerType,
        @NotNull UUID seatId,
        @NotBlank String seatNumber
) {
}
