package com.example.carsharingservice.dto.payment;

import com.example.carsharingservice.model.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePaymentRequestDto(
        @Positive
        Long rentalId,
        @NotNull
        PaymentType type
) {
}
