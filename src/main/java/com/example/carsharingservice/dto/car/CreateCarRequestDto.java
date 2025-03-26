package com.example.carsharingservice.dto.car;

import com.example.carsharingservice.model.CarType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateCarRequestDto(
        @NotBlank
        String model,
        @NotBlank
        String brand,
        @NotNull
        CarType type,
        @Positive
        int inventory,
        @Positive
        BigDecimal dailyFee
) {
}
