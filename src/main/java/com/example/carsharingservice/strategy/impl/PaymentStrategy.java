package com.example.carsharingservice.strategy.impl;

import com.example.carsharingservice.model.Rental;
import com.example.carsharingservice.strategy.PaymentCalculationStrategy;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class PaymentStrategy implements PaymentCalculationStrategy {
    @Override
    public BigDecimal calculateAmount(Rental rental) {
        long rentalTime = ChronoUnit.DAYS.between(rental.getRentalDate(), rental.getReturnDate());
        return BigDecimal.valueOf(rentalTime)
                .multiply(rental.getCar().getDailyFee());
    }
}
