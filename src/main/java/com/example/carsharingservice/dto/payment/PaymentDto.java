package com.example.carsharingservice.dto.payment;

import java.math.BigDecimal;
import java.net.URL;

public record PaymentDto(
        Long id,
        String type,
        Long rentalId,
        BigDecimal amount,
        URL sessionUrl,
        String sessionId
) {
}
