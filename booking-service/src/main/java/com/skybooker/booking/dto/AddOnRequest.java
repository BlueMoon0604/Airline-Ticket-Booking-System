package com.skybooker.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AddOnRequest(@NotBlank String mealPreference,@Min(0) Integer luggageKg) {
}
