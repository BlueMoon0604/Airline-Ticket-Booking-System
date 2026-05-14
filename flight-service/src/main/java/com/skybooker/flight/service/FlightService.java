package com.skybooker.flight.service;

import com.skybooker.flight.dto.ApiResponse;
import com.skybooker.flight.dto.FlightRequest;
import com.skybooker.flight.dto.FlightResponse;
import com.skybooker.flight.dto.FlightSearchRequest;
import com.skybooker.flight.dto.RoundTripSearchRequest;
import com.skybooker.flight.dto.RoundTripSearchResponse;
import com.skybooker.flight.dto.SeatCountUpdateRequest;
import com.skybooker.flight.dto.UpdateFlightStatusRequest;

import java.util.List;
import java.util.UUID;

public interface FlightService {

    FlightResponse addFlight(FlightRequest request);

    FlightResponse getFlightById(UUID flightId);

    FlightResponse getFlightByNumber(String flightNumber);

    List<FlightResponse> getFlightsByAirline(UUID airlineId);

    List<FlightResponse> getAllFlights();

    List<FlightResponse> searchFlights(FlightSearchRequest request);

    RoundTripSearchResponse searchRoundTrip(RoundTripSearchRequest request);

    FlightResponse updateFlight(UUID flightId, FlightRequest request);

    FlightResponse updateStatus(UUID flightId, UpdateFlightStatusRequest request);

    ApiResponse decrementSeats(UUID flightId, SeatCountUpdateRequest request);

    ApiResponse incrementSeats(UUID flightId, SeatCountUpdateRequest request);

    ApiResponse deleteFlight(UUID flightId);
}
