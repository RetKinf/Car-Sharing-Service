package com.example.carsharingservice.dto.rental;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RentalReturnDateDto(
        @NotNull
        LocalDate actualReturnDate
) {
}
