package com.skybooker.passenger.service;

import com.skybooker.passenger.dto.PassengerRequest;
import com.skybooker.passenger.entity.Gender;
import com.skybooker.passenger.entity.PassengerInfo;
import com.skybooker.passenger.entity.PassengerType;
import com.skybooker.passenger.exception.BadRequestException;
import com.skybooker.passenger.repository.PassengerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PassengerServiceImplTest {

    @Mock
    private PassengerRepository passengerRepository;

    @InjectMocks
    private PassengerServiceImpl passengerService;

    private PassengerRequest adultRequest;

    @BeforeEach
    void setUp() {
        adultRequest = new PassengerRequest(
                UUID.randomUUID(),
                "Ms",
                "Ananya",
                "Sharma",
                LocalDate.now().minusYears(24),
                Gender.FEMALE,
                "p1234567",
                "INDIAN",
                LocalDate.now().plusYears(5),
                PassengerType.ADULT
        );
    }

    @Test
    void addPassengerShouldGenerateTicketAndUppercasePassport() {
        when(passengerRepository.findByPassportNumber("p1234567")).thenReturn(Optional.empty());
        when(passengerRepository.findByTicketNumber(any())).thenReturn(Optional.empty());
        when(passengerRepository.save(any(PassengerInfo.class))).thenAnswer(invocation -> {
            PassengerInfo passenger = invocation.getArgument(0);
            passenger.setPassengerId(UUID.randomUUID());
            return passenger;
        });

        var response = passengerService.addPassenger(adultRequest);

        ArgumentCaptor<PassengerInfo> captor = ArgumentCaptor.forClass(PassengerInfo.class);
        verify(passengerRepository).save(captor.capture());
        PassengerInfo savedPassenger = captor.getValue();

        assertThat(savedPassenger.getPassportNumber()).isEqualTo("P1234567");
        assertThat(savedPassenger.getTicketNumber()).startsWith("TKT");
        assertThat(response.passportNumber()).isEqualTo("P1234567");
    }

    @Test
    void validatePassengerDataShouldRejectAdultBelowTwelveYears() {
        PassengerRequest invalidAdult = new PassengerRequest(
                UUID.randomUUID(),
                "Master",
                "Kid",
                "Sharma",
                LocalDate.now().minusYears(10),
                Gender.MALE,
                "P7654321",
                "INDIAN",
                LocalDate.now().plusYears(5),
                PassengerType.ADULT
        );

        assertThrows(BadRequestException.class, () -> passengerService.validatePassengerData(invalidAdult));
    }
}
