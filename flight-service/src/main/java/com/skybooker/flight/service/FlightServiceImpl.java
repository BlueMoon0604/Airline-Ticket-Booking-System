package com.skybooker.flight.service;

import com.skybooker.flight.dto.ApiResponse;
import com.skybooker.flight.dto.FlightRequest;
import com.skybooker.flight.dto.FlightResponse;
import com.skybooker.flight.dto.FlightSearchRequest;
import com.skybooker.flight.dto.RoundTripSearchRequest;
import com.skybooker.flight.dto.RoundTripSearchResponse;
import com.skybooker.flight.dto.SeatCountUpdateRequest;
import com.skybooker.flight.dto.UpdateFlightStatusRequest;
import com.skybooker.flight.entity.Flight;
import com.skybooker.flight.exception.BadRequestException;
import com.skybooker.flight.exception.ResourceNotFoundException;
import com.skybooker.flight.messaging.FlightEventPublisher;
import com.skybooker.flight.repository.FlightRepository;
import com.skybooker.flight.service.FlightService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final FlightEventPublisher flightEventPublisher;
    public FlightServiceImpl(FlightRepository flightRepository,
            FlightEventPublisher flightEventPublisher) {
            this.flightRepository = flightRepository;
            this.flightEventPublisher = flightEventPublisher;
}



 

    @Override
    public FlightResponse addFlight(FlightRequest request) {
        validateFlightRequest(request);

        if (flightRepository.findByFlightNumberIgnoreCase(request.flightNumber()).isPresent()) {
            throw new BadRequestException("Flight number already exists");
        }

        Flight flight = new Flight();
        mapRequestToEntity(request, flight);
        flight.setAvailableSeats(request.totalSeats());

        return mapToResponse(flightRepository.save(flight));
    }

    @Override
    @Transactional(readOnly = true)
    public FlightResponse getFlightById(UUID flightId) {
        return mapToResponse(findFlight(flightId));
    }

    @Override
    @Transactional(readOnly = true)
    public FlightResponse getFlightByNumber(String flightNumber) {
        Flight flight = flightRepository.findByFlightNumberIgnoreCase(flightNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
        return mapToResponse(flight);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightResponse> getFlightsByAirline(UUID airlineId) {
        return flightRepository.findByAirlineId(airlineId).stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightResponse> getAllFlights() {
        return flightRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightResponse> searchFlights(FlightSearchRequest request) {
        LocalDateTime start = request.departureDate().atStartOfDay();
        LocalDateTime end = request.departureDate().atTime(23, 59, 59);

        return flightRepository.findByOriginAirportCodeIgnoreCaseAndDestinationAirportCodeIgnoreCaseAndDepartureTimeBetween(
                        request.originAirportCode(),
                        request.destinationAirportCode(),
                        start,
                        end
                ).stream()
                .filter(flight -> flight.getAvailableSeats() >= request.passengers())
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoundTripSearchResponse searchRoundTrip(RoundTripSearchRequest request) {
        FlightSearchRequest outbound = new FlightSearchRequest(
                request.originAirportCode(),
                request.destinationAirportCode(),
                request.departureDate(),
                request.passengers()
        );

        FlightSearchRequest inbound = new FlightSearchRequest(
                request.destinationAirportCode(),
                request.originAirportCode(),
                request.returnDate(),
                request.passengers()
        );

        return new RoundTripSearchResponse(searchFlights(outbound), searchFlights(inbound));
    }

    @Override
    public FlightResponse updateFlight(UUID flightId, FlightRequest request) {
        validateFlightRequest(request);

        Flight flight = findFlight(flightId);
        Flight existing = flightRepository.findByFlightNumberIgnoreCase(request.flightNumber()).orElse(null);
        if (existing != null && !existing.getFlightId().equals(flightId)) {
            throw new BadRequestException("Flight number already exists");
        }

        int bookedSeats = flight.getTotalSeats() - flight.getAvailableSeats();
        if (request.totalSeats() < bookedSeats) {
            throw new BadRequestException("Total seats cannot be less than already booked seats");
        }

        mapRequestToEntity(request, flight);
        flight.setAvailableSeats(request.totalSeats() - bookedSeats);
        flightEventPublisher.publishFlightStatusUpdated(flight);


        return mapToResponse(flightRepository.save(flight));
    }

    @Override
    public FlightResponse updateStatus(UUID flightId, UpdateFlightStatusRequest request) {
        Flight flight = findFlight(flightId);
        flight.setStatus(request.status());
        return mapToResponse(flightRepository.save(flight));
    }

    @Override
    public ApiResponse decrementSeats(UUID flightId, SeatCountUpdateRequest request) {
        Flight flight = findFlight(flightId);
        if (flight.getAvailableSeats() < request.seats()) {
            throw new BadRequestException("Not enough available seats");
        }
        flight.setAvailableSeats(flight.getAvailableSeats() - request.seats());
        flightRepository.save(flight);
        return new ApiResponse("Seats decremented successfully", mapToResponse(flight));
    }

    @Override
    public ApiResponse incrementSeats(UUID flightId, SeatCountUpdateRequest request) {
        Flight flight = findFlight(flightId);
        if (flight.getAvailableSeats() + request.seats() > flight.getTotalSeats()) {
            throw new BadRequestException("Available seats cannot exceed total seats");
        }
        flight.setAvailableSeats(flight.getAvailableSeats() + request.seats());
        flightRepository.save(flight);
        return new ApiResponse("Seats incremented successfully", mapToResponse(flight));
    }

    @Override
    public ApiResponse deleteFlight(UUID flightId) {
        Flight flight = findFlight(flightId);
        flightRepository.delete(flight);
        return new ApiResponse("Flight deleted successfully");
    }

    private Flight findFlight(UUID flightId) {
        return flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
    }

    private void validateFlightRequest(FlightRequest request) {
        if (request.originAirportCode().equalsIgnoreCase(request.destinationAirportCode())) {
            throw new BadRequestException("Origin and destination cannot be same");
        }
        if (!request.arrivalTime().isAfter(request.departureTime())) {
            throw new BadRequestException("Arrival time must be after departure time");
        }
    }

    private void mapRequestToEntity(FlightRequest request, Flight flight) {
        flight.setFlightNumber(request.flightNumber().toUpperCase());
        flight.setAirlineId(request.airlineId());
        flight.setOriginAirportCode(request.originAirportCode().toUpperCase());
        flight.setDestinationAirportCode(request.destinationAirportCode().toUpperCase());
        flight.setDepartureTime(request.departureTime());
        flight.setArrivalTime(request.arrivalTime());
        flight.setDurationMinutes(request.durationMinutes());
        flight.setAircraftType(request.aircraftType());
        flight.setTotalSeats(request.totalSeats());
        flight.setBasePrice(request.basePrice());
    }

    private FlightResponse mapToResponse(Flight flight) {
        return new FlightResponse(
                flight.getFlightId(),
                flight.getFlightNumber(),
                flight.getAirlineId(),
                flight.getOriginAirportCode(),
                flight.getDestinationAirportCode(),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                flight.getDurationMinutes(),
                flight.getStatus(),
                flight.getAircraftType(),
                flight.getTotalSeats(),
                flight.getAvailableSeats(),
                flight.getBasePrice()
        );
    }
}
