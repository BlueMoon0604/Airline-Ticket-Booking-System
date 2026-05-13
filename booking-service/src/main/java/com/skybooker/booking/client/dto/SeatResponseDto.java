package com.skybooker.booking.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SeatResponseDto(
        UUID seatId,
        UUID flightId,
        String seatNumber,
        String seatClass,
        Integer rowNumber,
        String seatColumn,
        Boolean isWindow,
        Boolean isAisle,
        Boolean hasExtraLegroom,
        String status,
        BigDecimal priceMultiplier,
        LocalDateTime holdUntil
) {
}
