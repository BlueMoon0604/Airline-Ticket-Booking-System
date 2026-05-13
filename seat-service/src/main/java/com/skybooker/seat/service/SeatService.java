package com.skybooker.seat.service;

import com.skybooker.seat.dto.ApiResponse;
import com.skybooker.seat.dto.SeatCountByClassResponse;
import com.skybooker.seat.dto.SeatMapResponse;
import com.skybooker.seat.dto.SeatRequest;
import com.skybooker.seat.dto.SeatResponse;
import com.skybooker.seat.entity.SeatClass;

import java.util.List;
import java.util.UUID;

public interface SeatService {
    SeatResponse addSeat(SeatRequest request);
    List<SeatResponse> addSeatsForFlight(List<SeatRequest> requests);
    List<SeatResponse> getSeatsByFlight(UUID flightId);
    List<SeatResponse> getAvailableSeats(UUID flightId);
    List<SeatResponse> getAvailableByClass(UUID flightId, SeatClass seatClass);

    SeatResponse getSeatById(UUID seatId);
    SeatMapResponse getSeatMap(UUID flightId);
    ApiResponse holdSeat(UUID seatId);
    ApiResponse releaseSeat(UUID seatId);
    ApiResponse confirmSeat(UUID seatId);

    SeatResponse updateSeat(UUID seatId, SeatRequest request);
    List<SeatCountByClassResponse> countAvailableByClass(UUID flightId);
    ApiResponse deleteSeatsForFlight(UUID flightId);
    ApiResponse releaseExpiredHolds();
}
