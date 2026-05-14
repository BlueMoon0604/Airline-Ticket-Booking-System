package com.skybooker.seat.dto;

import java.util.List;
import java.util.UUID;

public record SeatMapResponse(UUID flightId,List<SeatResponse> seats) {
	
}
