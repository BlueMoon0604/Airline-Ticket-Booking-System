package com.skybooker.seat.dto;

import com.skybooker.seat.entity.SeatClass;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record SeatRequest(@NotNull UUID flightId,@NotBlank String seatNumber,@NotNull SeatClass seatClass,@NotNull @Min(1) Integer rowNumber,
        @NotBlank String seatColumn,
        @NotNull Boolean isWindow,
        @NotNull Boolean isAisle,
        @NotNull Boolean hasExtraLegroom,
        @NotNull @DecimalMin("0.1") BigDecimal priceMultiplier) {
	
}
