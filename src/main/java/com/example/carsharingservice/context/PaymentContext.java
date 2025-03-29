package com.example.carsharingservice.context;

import com.example.carsharingservice.model.PaymentType;
import com.example.carsharingservice.model.Rental;
import com.example.carsharingservice.strategy.PaymentCalculationStrategy;
import com.example.carsharingservice.strategy.impl.FineStrategy;
import com.example.carsharingservice.strategy.impl.PaymentStrategy;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PaymentContext {
    private final Map<PaymentType, PaymentCalculationStrategy> strategies;

    public PaymentContext() {
        this.strategies = new HashMap<>();
        this.strategies.put(PaymentType.PAYMENT, new PaymentStrategy());
        this.strategies.put(PaymentType.FINE, new FineStrategy());
    }

    public BigDecimal calculateAmount(Rental rental, PaymentType paymentType) {
        PaymentCalculationStrategy strategy = strategies.get(paymentType);
        return strategy.calculateAmount(rental);
    }
}
