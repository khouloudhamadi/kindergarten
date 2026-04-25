package com.kindergarten.kindergarten.parent;



import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategies;

    public PaymentStrategyFactory(Map<String, PaymentStrategy> strategies) {
        this.strategies = strategies;
    }

    public PaymentStrategy getStrategy(String strategyType) {
        PaymentStrategy strategy = strategies.get(strategyType);
        if (strategy == null) {
            throw new IllegalArgumentException("Stratégie inconnue : " + strategyType);
        }
        return strategy;
    }
}