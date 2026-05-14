package com.skybooker.booking.dto;

import com.skybooker.booking.entity.TripType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateBookingRequest(
        @NotNull UUID userId,
        @NotNull UUID flightId,
        @NotNull TripType tripType,
        String mealPreference,
        @NotNull @Min(0) Integer luggageKg,
        @NotBlank @Email String contactEmail,
        @NotBlank String contactPhone,
        @NotBlank String paymentMode,
        @NotEmpty List<PassengerBookingRequest> passengers
) {
}
