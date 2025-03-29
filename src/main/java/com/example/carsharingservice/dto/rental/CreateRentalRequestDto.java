package com.example.carsharingservice.dto.rental;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record CreateRentalRequestDto(
        @NotNull
        LocalDate returnDate,
        @NotNull
        @Positive
        Long carId
) {
}
