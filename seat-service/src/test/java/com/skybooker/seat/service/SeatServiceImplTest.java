package com.skybooker.seat.service;

import com.skybooker.seat.dto.SeatRequest;
import com.skybooker.seat.entity.Seat;
import com.skybooker.seat.entity.SeatClass;
import com.skybooker.seat.entity.SeatStatus;
import com.skybooker.seat.exception.BadRequestException;
import com.skybooker.seat.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeatServiceImplTest {

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private SeatServiceImpl seatService;

    private SeatRequest seatRequest;

    @BeforeEach
    void setUp() {
        seatRequest = new SeatRequest(
                UUID.randomUUID(),
                "15C",
                SeatClass.ECONOMY,
                15,
                "C",
                false,
                true,
                false,
                BigDecimal.ONE
        );
    }

    @Test
    void holdSeatShouldReclaimExpiredHeldSeat() {
        UUID seatId = UUID.randomUUID();
        Seat seat = new Seat();
        seat.setSeatId(seatId);
        seat.setFlightId(seatRequest.flightId());
        seat.setSeatNumber("15C");
        seat.setSeatClass(SeatClass.ECONOMY);
        seat.setRowNumber(15);
        seat.setSeatColumn("C");
        seat.setStatus(SeatStatus.HELD);
        seat.setHoldUntil(LocalDateTime.now().minusMinutes(1));

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(seatRepository.save(org.mockito.ArgumentMatchers.any(Seat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = seatService.holdSeat(seatId);

        assertThat(((com.skybooker.seat.dto.SeatResponse) response.data()).status()).isEqualTo(SeatStatus.HELD);
        assertThat(seat.getHoldUntil()).isAfter(LocalDateTime.now());
    }

    @Test
    void confirmSeatShouldThrowWhenSeatIsNotHeld() {
        UUID seatId = UUID.randomUUID();
        Seat seat = new Seat();
        seat.setSeatId(seatId);
        seat.setStatus(SeatStatus.AVAILABLE);

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        assertThrows(BadRequestException.class, () -> seatService.confirmSeat(seatId));
    }
}
