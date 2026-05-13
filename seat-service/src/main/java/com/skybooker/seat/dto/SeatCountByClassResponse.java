package com.skybooker.seat.dto;

import com.skybooker.seat.entity.SeatClass;

public record SeatCountByClassResponse(SeatClass seatClass,long availableSeats) {
	
}
