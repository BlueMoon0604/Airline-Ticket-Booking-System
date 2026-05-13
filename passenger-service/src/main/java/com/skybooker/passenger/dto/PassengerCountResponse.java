package com.skybooker.passenger.dto;

import java.util.UUID;

public record PassengerCountResponse(UUID bookingId,long totalPassengers) {
	
}
 