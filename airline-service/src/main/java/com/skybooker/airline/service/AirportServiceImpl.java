package com.skybooker.airline.service;

import com.skybooker.airline.dto.AirportRequest;
import com.skybooker.airline.dto.AirportResponse;
import com.skybooker.airline.entity.Airport;
import com.skybooker.airline.exception.BadRequestException;
import com.skybooker.airline.exception.ResourceNotFoundException;
import com.skybooker.airline.repository.AirportRepository;
import com.skybooker.airline.service.AirportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AirportServiceImpl implements AirportService {

    private final AirportRepository airportRepository;

    public AirportServiceImpl(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    @Override
    public AirportResponse createAirport(AirportRequest request) {
        if (airportRepository.existsByIataCodeIgnoreCase(request.iataCode())) {
            throw new BadRequestException("IATA code already exists");
        }
        if (request.icaoCode() != null && !request.icaoCode().isBlank()
                && airportRepository.existsByIcaoCodeIgnoreCase(request.icaoCode())) {
            throw new BadRequestException("ICAO code already exists");
        }

        Airport airport = new Airport();
        mapRequestToEntity(request, airport);
        return mapToResponse(airportRepository.save(airport));
    }

    @Override
    @Transactional(readOnly = true)
    public AirportResponse getAirportById(UUID airportId) {
        return mapToResponse(findAirport(airportId));
    }

    @Override
    @Transactional(readOnly = true)
    public AirportResponse getAirportByIata(String iataCode) {
        Airport airport = airportRepository.findByIataCodeIgnoreCase(iataCode)
                .orElseThrow(() -> new ResourceNotFoundException("Airport not found"));
        return mapToResponse(airport);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AirportResponse> getAllAirports() {
        return airportRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AirportResponse> getAirportsByCity(String city) {
        return airportRepository.findByCityIgnoreCase(city).stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AirportResponse> getAirportsByCountry(String country) {
        return airportRepository.findByCountryIgnoreCase(country).stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AirportResponse> searchAirports(String keyword) {
        return airportRepository
                .findByNameContainingIgnoreCaseOrCityContainingIgnoreCaseOrIataCodeContainingIgnoreCase(
                        keyword, keyword, keyword
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public AirportResponse updateAirport(UUID airportId, AirportRequest request) {
        Airport airport = findAirport(airportId);

        Airport existingIata = airportRepository.findByIataCodeIgnoreCase(request.iataCode()).orElse(null);
        if (existingIata != null && !existingIata.getAirportId().equals(airportId)) {
            throw new BadRequestException("IATA code already exists");
        }

        if (request.icaoCode() != null && !request.icaoCode().isBlank()) {
            Airport existingIcao = airportRepository.findByIcaoCodeIgnoreCase(request.icaoCode()).orElse(null);
            if (existingIcao != null && !existingIcao.getAirportId().equals(airportId)) {
                throw new BadRequestException("ICAO code already exists");
            }
        }

        mapRequestToEntity(request, airport);
        return mapToResponse(airportRepository.save(airport));
    }

    private Airport findAirport(UUID airportId) {
        return airportRepository.findById(airportId)
                .orElseThrow(() -> new ResourceNotFoundException("Airport not found"));
    }

    private void mapRequestToEntity(AirportRequest request, Airport airport) {
        airport.setName(request.name());
        airport.setIataCode(request.iataCode().toUpperCase());
        airport.setIcaoCode(request.icaoCode() == null ? null : request.icaoCode().toUpperCase());
        airport.setCity(request.city());
        airport.setCountry(request.country());
        airport.setLatitude(request.latitude());
        airport.setLongitude(request.longitude());
        airport.setTimezone(request.timezone());
    }

    private AirportResponse mapToResponse(Airport airport) {
        return new AirportResponse(
                airport.getAirportId(),
                airport.getName(),
                airport.getIataCode(),
                airport.getIcaoCode(),
                airport.getCity(),
                airport.getCountry(),
                airport.getLatitude(),
                airport.getLongitude(),
                airport.getTimezone()
        );
    }
}
