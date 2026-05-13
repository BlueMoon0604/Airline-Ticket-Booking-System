package com.skybooker.seat.dto;

import com.skybooker.seat.entity.SeatClass;
import com.skybooker.seat.entity.SeatStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SeatResponse(UUID seatId,UUID flightId,String seatNumber,SeatClass seatClass,Integer rowNumber,String seatColumn,Boolean isWindow,Boolean isAisle,Boolean hasExtraLegroom,
        SeatStatus status,
        BigDecimal priceMultiplier,
        LocalDateTime holdUntil) {
	
}
