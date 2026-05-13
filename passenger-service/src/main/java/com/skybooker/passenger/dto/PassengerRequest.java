package com.skybooker.passenger.dto;

import com.skybooker.passenger.entity.Gender;
import com.skybooker.passenger.entity.PassengerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record PassengerRequest(@NotNull UUID bookingId, @NotBlank String title, @NotBlank String firstName, @NotBlank String lastName,@NotNull LocalDate dateOfBirth,@NotNull Gender gender,@NotBlank String passportNumber,
        @NotBlank String nationality,
        @NotNull LocalDate passportExpiry,
        PassengerType passengerType) {
	
}
