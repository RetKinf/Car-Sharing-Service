package com.example.carsharingservice.dto.car;

import java.math.BigDecimal;

public record CarResponseDto(
        Long id,
        String model,
        String brand,
        String type,
        int inventory,
        BigDecimal dailyFee
) {
}
