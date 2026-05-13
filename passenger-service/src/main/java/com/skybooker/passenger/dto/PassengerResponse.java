package com.skybooker.passenger.dto;

import com.skybooker.passenger.entity.Gender;
import com.skybooker.passenger.entity.PassengerType;

import java.time.LocalDate;
import java.util.UUID;

public record PassengerResponse(UUID passengerId,UUID bookingId,String title,String firstName,String lastName,LocalDate dateOfBirth,Gender gender,String passportNumber,String nationality,
        LocalDate passportExpiry,
        UUID seatId,
        String seatNumber,
        String ticketNumber,
        PassengerType passengerType
) {
}
