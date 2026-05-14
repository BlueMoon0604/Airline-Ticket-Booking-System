package com.skybooker.airline.service;

import com.skybooker.airline.dto.AirportRequest;
import com.skybooker.airline.dto.AirportResponse;

import java.util.List;
import java.util.UUID;

public interface AirportService {

    AirportResponse createAirport(AirportRequest request);

    AirportResponse getAirportById(UUID airportId);

    AirportResponse getAirportByIata(String iataCode);

    List<AirportResponse> getAllAirports();

    List<AirportResponse> getAirportsByCity(String city);

    List<AirportResponse> getAirportsByCountry(String country);

    List<AirportResponse> searchAirports(String keyword);

    AirportResponse updateAirport(UUID airportId, AirportRequest request);
}
