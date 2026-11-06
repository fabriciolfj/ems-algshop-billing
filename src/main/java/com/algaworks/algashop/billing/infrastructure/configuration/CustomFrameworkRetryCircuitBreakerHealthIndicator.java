package com.algaworks.algashop.billing.infrastructure.configuration;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.cloud.circuitbreaker.retry.CircuitBreakerRetryPolicy;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.algaworks.algashop.billing.infrastructure.configuration.SpringCircuitBreakConfig.FAST_API_CB;


@Component("circuitBreakers")
public class CustomFrameworkRetryCircuitBreakerHealthIndicator implements HealthIndicator {

    private final List<FrameworkRetryCircuitBreaker> circuitBreakers = new ArrayList<>();

    public CustomFrameworkRetryCircuitBreakerHealthIndicator(final CircuitBreakerFactory circuitBreakerFactory) {
        circuitBreakers.add((FrameworkRetryCircuitBreaker) circuitBreakerFactory.create(FAST_API_CB));
    }

    @Override
    public @Nullable Health health() {
        Map<String, Object> indicatorDetails = new HashMap<>();
        String indicatorStatus = "UP";
        Throwable lastException = null;

        for (FrameworkRetryCircuitBreaker circuit: circuitBreakers) {
           var policy = circuit.getConfig().getCircuitBreakerRetryPolicy();
           var state = policy.getState();
           Map<String, Object> cbDetails = new HashMap<>();
            cbDetails.put("state", state.name());

           if  (state == CircuitBreakerRetryPolicy.State.OPEN) {
               indicatorStatus = "DEGRADED";

               if (policy.getLastException() != null
                       && policy.getLastException().getCause() != null) {
                   lastException = policy.getLastException().getCause();
                   cbDetails.put("lastException", lastException.getMessage());
               } else {
                   cbDetails.put("lastException", null);
               }
           }

            indicatorDetails.put(circuit.getId(), cbDetails);
        }

        Health.Builder builder = Health.status(indicatorStatus).withDetails(indicatorDetails);
        if (indicatorStatus.equals("DEGRADED") && lastException != null) {
            builder.withException(lastException);
        }

        return builder.build();
    }
}
