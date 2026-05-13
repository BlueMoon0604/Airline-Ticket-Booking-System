package com.skybooker.passenger.service;

import com.skybooker.passenger.dto.ApiResponse;
import com.skybooker.passenger.dto.PassengerCountResponse;
import com.skybooker.passenger.dto.PassengerRequest;
import com.skybooker.passenger.dto.PassengerResponse;
import com.skybooker.passenger.dto.SeatAssignmentRequest;

import java.util.List;
import java.util.UUID;

public interface PassengerService {

    PassengerResponse addPassenger(PassengerRequest request);
    PassengerResponse getPassengerById(UUID passengerId);
    List<PassengerResponse> getPassengersByBooking(UUID bookingId);
    PassengerResponse getByPassportNumber(String passportNumber);

    PassengerResponse updatePassenger(UUID passengerId, PassengerRequest request);

    ApiResponse assignSeat(UUID passengerId, SeatAssignmentRequest request);
    ApiResponse deletePassenger(UUID passengerId);
    ApiResponse deletePassengersByBooking(UUID bookingId);
    PassengerCountResponse getPassengerCount(UUID bookingId);

    void validatePassengerData(PassengerRequest request);
}
