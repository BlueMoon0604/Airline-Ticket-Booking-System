package com.skybooker.booking.dto;

import java.math.BigDecimal;

public record FareSummaryResponse(BigDecimal baseFare,BigDecimal gstAmount,BigDecimal fuelSurcharge,BigDecimal ancillaryAmount,
        BigDecimal totalFare) {
}
