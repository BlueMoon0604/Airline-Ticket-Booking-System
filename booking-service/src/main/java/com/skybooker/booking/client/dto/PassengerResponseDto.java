package com.skybooker.booking.client.dto;

import java.util.UUID;

public record PassengerResponseDto(
        UUID passengerId,
        UUID bookingId,
        String title,
        String firstName,
        String lastName,
        String dateOfBirth,
        String gender,
        String passportNumber,
        String nationality,
        String passportExpiry,
        UUID seatId,
        String seatNumber,
        String ticketNumber,
        String passengerType
) {
}
