package com.skybooker.booking.client.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PassengerRequestDto(
        UUID bookingId,
        String title,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        String passportNumber,
        String nationality,
        LocalDate passportExpiry,
        String passengerType
) {
}
