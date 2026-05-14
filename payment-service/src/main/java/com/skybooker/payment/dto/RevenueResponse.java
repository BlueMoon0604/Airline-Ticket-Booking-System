package com.skybooker.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RevenueResponse(
        BigDecimal totalRevenue,
        LocalDateTime from,
        LocalDateTime to
) {
}
