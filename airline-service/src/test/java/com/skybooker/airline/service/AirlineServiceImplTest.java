package com.skybooker.airline.service;

import com.skybooker.airline.dto.AirlineRequest;
import com.skybooker.airline.entity.Airline;
import com.skybooker.airline.exception.BadRequestException;
import com.skybooker.airline.repository.AirlineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirlineServiceImplTest {

    @Mock
    private AirlineRepository airlineRepository;

    @InjectMocks
    private AirlineServiceImpl airlineService;

    private AirlineRequest request;

    @BeforeEach
    void setUp() {
        request = new AirlineRequest(
                "IndiGo",
                "6e",
                "igo",
                null,
                "India",
                "support@indigo.com",
                "1800123456"
        );
    }

    @Test
    void createAirlineShouldUppercaseCodesAndActivateAirline() {
        when(airlineRepository.existsByIataCodeIgnoreCase("6e")).thenReturn(false);
        when(airlineRepository.existsByIcaoCodeIgnoreCase("igo")).thenReturn(false);
        when(airlineRepository.save(any(Airline.class))).thenAnswer(invocation -> {
            Airline saved = invocation.getArgument(0);
            saved.setAirlineId(UUID.randomUUID());
            return saved;
        });

        var response = airlineService.createAirline(request);

        ArgumentCaptor<Airline> captor = ArgumentCaptor.forClass(Airline.class);
        verify(airlineRepository).save(captor.capture());
        Airline savedAirline = captor.getValue();

        assertThat(savedAirline.getIataCode()).isEqualTo("6E");
        assertThat(savedAirline.getIcaoCode()).isEqualTo("IGO");
        assertThat(savedAirline.getIsActive()).isTrue();
        assertThat(response.iataCode()).isEqualTo("6E");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void createAirlineShouldThrowWhenIataAlreadyExists() {
        when(airlineRepository.existsByIataCodeIgnoreCase("6e")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> airlineService.createAirline(request));
    }
}
