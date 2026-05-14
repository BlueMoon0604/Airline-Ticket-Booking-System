package com.skybooker.flight.service;

import com.skybooker.flight.dto.FlightRequest;

import com.skybooker.flight.dto.SeatCountUpdateRequest;
import com.skybooker.flight.entity.Flight;
import com.skybooker.flight.exception.BadRequestException;
import com.skybooker.flight.messaging.FlightEventPublisher;
import com.skybooker.flight.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightServiceImplTest {

    @Mock
    private FlightRepository flightRepository;
    @Mock
    private FlightEventPublisher flightEventPublisher;

    @InjectMocks
    private FlightServiceImpl flightService;

    private FlightRequest request;

    @BeforeEach
    void setUp() {
        request = new FlightRequest(
                "6E101",
                UUID.randomUUID(),
                "DEL",
                "BOM",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(2),
                120,
                "Airbus A320",
                180,
                BigDecimal.valueOf(4500)
        );
    }

    @Test
    void addFlightShouldInitializeAvailableSeatsWithTotalSeats() {
        when(flightRepository.findByFlightNumberIgnoreCase("6E101")).thenReturn(Optional.empty());
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> {
            Flight flight = invocation.getArgument(0);
            flight.setFlightId(UUID.randomUUID());
            return flight;
        });

        var response = flightService.addFlight(request);

        ArgumentCaptor<Flight> captor = ArgumentCaptor.forClass(Flight.class);
        verify(flightRepository).save(captor.capture());
        assertThat(captor.getValue().getAvailableSeats()).isEqualTo(request.totalSeats());
        assertThat(response.availableSeats()).isEqualTo(request.totalSeats());
    }

    @Test
    void decrementSeatsShouldThrowWhenNotEnoughSeatsAvailable() {
        UUID flightId = UUID.randomUUID();
        Flight flight = new Flight();
        flight.setFlightId(flightId);
        flight.setAvailableSeats(1);
        flight.setTotalSeats(10);

        when(flightRepository.findById(flightId)).thenReturn(Optional.of(flight));

        assertThrows(
                BadRequestException.class,
                () -> flightService.decrementSeats(flightId, new SeatCountUpdateRequest(2))
        );
    }
}
