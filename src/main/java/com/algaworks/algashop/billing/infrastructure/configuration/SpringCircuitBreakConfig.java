package com.algaworks.algashop.billing.infrastructure.configuration;

import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;

import java.time.Duration;

@Configuration
public class SpringCircuitBreakConfig {

    public Customizer<FrameworkRetryCircuitBreakerFactory> circuitBreakerFactoryCustomizer() {
        final RetryPolicy policy = RetryPolicy
                .builder()
                .maxRetries(3)
                .multiplier(2)
                .delay(Duration.ofSeconds(3))
                .build();

        return factory -> {
            factory.configure(
                    buider -> buider
                            .retryPolicy(policy)
                            .resetTimeout(Duration.ofSeconds(25))
                            .openTimeout(Duration.ofSeconds(10))
                            .build(), "faspayCB"
            );
        };
    }
}
