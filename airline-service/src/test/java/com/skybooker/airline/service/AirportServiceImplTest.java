package com.skybooker.airline.service;

import com.skybooker.airline.dto.AirportRequest;
import com.skybooker.airline.entity.Airport;
import com.skybooker.airline.exception.BadRequestException;
import com.skybooker.airline.repository.AirportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirportServiceImplTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private AirportServiceImpl airportService;

    private AirportRequest request;

    @BeforeEach
    void setUp() {
        request = new AirportRequest(
                "Indira Gandhi International Airport",
                "del",
                "vidp",
                "Delhi",
                "India",
                28.5562,
                77.1000,
                "Asia/Kolkata"
        );
    }

    @Test
    void createAirportShouldUppercaseCodes() {
        when(airportRepository.existsByIataCodeIgnoreCase("del")).thenReturn(false);
        when(airportRepository.existsByIcaoCodeIgnoreCase("vidp")).thenReturn(false);
        when(airportRepository.save(any(Airport.class))).thenAnswer(invocation -> {
            Airport airport = invocation.getArgument(0);
            airport.setAirportId(UUID.randomUUID());
            return airport;
        });

        var response = airportService.createAirport(request);

        ArgumentCaptor<Airport> captor = ArgumentCaptor.forClass(Airport.class);
        verify(airportRepository).save(captor.capture());
        assertThat(captor.getValue().getIataCode()).isEqualTo("DEL");
        assertThat(captor.getValue().getIcaoCode()).isEqualTo("VIDP");
        assertThat(response.iataCode()).isEqualTo("DEL");
    }

    @Test
    void updateAirportShouldThrowWhenIataBelongsToAnotherAirport() {
        UUID airportId = UUID.randomUUID();
        Airport existingAirport = new Airport();
        existingAirport.setAirportId(airportId);

        Airport conflictingAirport = new Airport();
        conflictingAirport.setAirportId(UUID.randomUUID());

        when(airportRepository.findById(airportId)).thenReturn(Optional.of(existingAirport));
        when(airportRepository.findByIataCodeIgnoreCase("del")).thenReturn(Optional.of(conflictingAirport));

        assertThrows(BadRequestException.class, () -> airportService.updateAirport(airportId, request));
    }
}
