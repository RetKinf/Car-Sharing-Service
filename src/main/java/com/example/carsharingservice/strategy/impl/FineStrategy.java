package com.example.carsharingservice.strategy.impl;

import com.example.carsharingservice.model.Rental;
import com.example.carsharingservice.strategy.PaymentCalculationStrategy;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class FineStrategy implements PaymentCalculationStrategy {
    private static final BigDecimal FINE_MULTIPLIER = BigDecimal.valueOf(2.0);

    @Override
    public BigDecimal calculateAmount(Rental rental) {
        long rentalTime = ChronoUnit.DAYS.between(
                rental.getRentalDate(),
                rental.getActualReturnDate()
        );
        long fineRentalTime = ChronoUnit.DAYS.between(
                rental.getReturnDate(),
                rental.getActualReturnDate()
        );
        BigDecimal paymentAmount = BigDecimal.valueOf(rentalTime)
                .multiply(rental.getCar().getDailyFee());
        BigDecimal fineAmount = BigDecimal.valueOf(fineRentalTime)
                .multiply(rental.getCar().getDailyFee())
                .multiply(FINE_MULTIPLIER);
        return paymentAmount.add(fineAmount);
    }
}
