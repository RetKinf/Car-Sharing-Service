package com.example.carsharingservice.config;

import com.example.carsharingservice.model.PaymentType;
import com.example.carsharingservice.strategy.PaymentCalculationStrategy;
import com.example.carsharingservice.strategy.impl.FineStrategy;
import com.example.carsharingservice.strategy.impl.PaymentStrategy;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StrategyConfig {

    @Bean
    public Map<PaymentType, PaymentCalculationStrategy> strategies(
            PaymentStrategy paymentStrategy,
            FineStrategy fineStrategy) {
        Map<PaymentType, PaymentCalculationStrategy> strategies = new HashMap<>();
        strategies.put(PaymentType.PAYMENT, paymentStrategy);
        strategies.put(PaymentType.FINE, fineStrategy);
        return strategies;
    }
}
