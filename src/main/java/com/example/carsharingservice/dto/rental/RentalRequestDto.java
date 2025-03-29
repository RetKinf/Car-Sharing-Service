package com.example.carsharingservice.dto.rental;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RentalRequestDto(
        @NotNull
        @Positive
        Long userId,
        boolean isActive
) {
}
