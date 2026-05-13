package com.skybooker.airline.service;

import com.skybooker.airline.dto.AirlineRequest;
import com.skybooker.airline.dto.AirlineResponse;
import com.skybooker.airline.dto.ApiResponse;

import java.util.List;
import java.util.UUID;

public interface AirlineService {

    AirlineResponse createAirline(AirlineRequest request);

    AirlineResponse getAirlineById(UUID airlineId);

    AirlineResponse getAirlineByIata(String iataCode);

    List<AirlineResponse> getAllAirlines();

    List<AirlineResponse> getActiveAirlines();

    AirlineResponse updateAirline(UUID airlineId, AirlineRequest request);

    ApiResponse deactivateAirline(UUID airlineId);

    ApiResponse activateAirline(UUID airlineId);
}
