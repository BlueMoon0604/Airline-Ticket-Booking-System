package com.skybooker.airline.service;

import com.skybooker.airline.dto.AirlineRequest;
import com.skybooker.airline.dto.AirlineResponse;
import com.skybooker.airline.dto.ApiResponse;
import com.skybooker.airline.entity.Airline;
import com.skybooker.airline.exception.BadRequestException;
import com.skybooker.airline.exception.ResourceNotFoundException;
import com.skybooker.airline.repository.AirlineRepository;
import com.skybooker.airline.service.AirlineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AirlineServiceImpl implements AirlineService {

    private final AirlineRepository airlineRepository;

    public AirlineServiceImpl(AirlineRepository airlineRepository) {
        this.airlineRepository = airlineRepository;
    }

    @Override
    public AirlineResponse createAirline(AirlineRequest request) {
        if (airlineRepository.existsByIataCodeIgnoreCase(request.iataCode())) {
            throw new BadRequestException("IATA code already exists");
        }
        if (request.icaoCode() != null && !request.icaoCode().isBlank()
                && airlineRepository.existsByIcaoCodeIgnoreCase(request.icaoCode())) {
            throw new BadRequestException("ICAO code already exists");
        }

        Airline airline = new Airline();
        mapRequestToEntity(request, airline);
        airline.setIsActive(true);

        return mapToResponse(airlineRepository.save(airline));
    }

    @Override
    @Transactional(readOnly = true)
    public AirlineResponse getAirlineById(UUID airlineId) {
        return mapToResponse(findAirline(airlineId));
    }

    @Override
    @Transactional(readOnly = true)
    public AirlineResponse getAirlineByIata(String iataCode) {
        Airline airline = airlineRepository.findByIataCodeIgnoreCase(iataCode)
                .orElseThrow(() -> new ResourceNotFoundException("Airline not found"));
        return mapToResponse(airline);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AirlineResponse> getAllAirlines() {
        return airlineRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AirlineResponse> getActiveAirlines() {
        return airlineRepository.findByIsActiveTrue().stream().map(this::mapToResponse).toList();
    }

    @Override
    public AirlineResponse updateAirline(UUID airlineId, AirlineRequest request) {
        Airline airline = findAirline(airlineId);

        Airline existingIata = airlineRepository.findByIataCodeIgnoreCase(request.iataCode()).orElse(null);
        if (existingIata != null && !existingIata.getAirlineId().equals(airlineId)) {
            throw new BadRequestException("IATA code already exists");
        }

        if (request.icaoCode() != null && !request.icaoCode().isBlank()) {
            Airline existingIcao = airlineRepository.findByIcaoCodeIgnoreCase(request.icaoCode()).orElse(null);
            if (existingIcao != null && !existingIcao.getAirlineId().equals(airlineId)) {
                throw new BadRequestException("ICAO code already exists");
            }
        }

        mapRequestToEntity(request, airline);
        return mapToResponse(airlineRepository.save(airline));
    }

    @Override
    public ApiResponse deactivateAirline(UUID airlineId) {
        Airline airline = findAirline(airlineId);
        airline.setIsActive(false);
        airlineRepository.save(airline);
        return new ApiResponse("Airline deactivated successfully", mapToResponse(airline));
    }

    @Override
    public ApiResponse activateAirline(UUID airlineId) {
        Airline airline = findAirline(airlineId);
        airline.setIsActive(true);
        airlineRepository.save(airline);
        return new ApiResponse("Airline activated successfully", mapToResponse(airline));
    }

    private Airline findAirline(UUID airlineId) {
        return airlineRepository.findById(airlineId)
                .orElseThrow(() -> new ResourceNotFoundException("Airline not found"));
    }

    private void mapRequestToEntity(AirlineRequest request, Airline airline) {
        airline.setName(request.name());
        airline.setIataCode(request.iataCode().toUpperCase());
        airline.setIcaoCode(request.icaoCode() == null ? null : request.icaoCode().toUpperCase());
        airline.setLogoUrl(request.logoUrl());
        airline.setCountry(request.country());
        airline.setContactEmail(request.contactEmail());
        airline.setContactPhone(request.contactPhone());
    }

    private AirlineResponse mapToResponse(Airline airline) {
        return new AirlineResponse(
                airline.getAirlineId(),
                airline.getName(),
                airline.getIataCode(),
                airline.getIcaoCode(),
                airline.getLogoUrl(),
                airline.getCountry(),
                airline.getContactEmail(),
                airline.getContactPhone(),
                airline.getIsActive()
        );
    }
}



