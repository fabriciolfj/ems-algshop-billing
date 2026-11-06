package com.algaworks.algashop.billing.infrastructure.creditcard.fastpay;

import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardProviderService;
import com.algaworks.algashop.billing.domain.model.creditcard.LimitedCreditCard;
import com.algaworks.algashop.billing.infrastructure.creditcard.fake.CreditCardProviderServiceFakeImpl;
import com.algaworks.algashop.billing.presentation.BadGatewayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import java.util.Optional;
import java.util.UUID;

import static com.algaworks.algashop.billing.infrastructure.configuration.SpringCircuitBreakConfig.FAST_API_CB;

@Slf4j
@Service
@ConditionalOnProperty(name = "algashop.integrations.payment.provider", havingValue = "FASTPAY")
public class CreditCardProviderServiceFastpayImpl implements CreditCardProviderService {

    private final FastpayCreditCardAPIClient fastpayCreditCardAPIClient;
    private final CircuitBreaker fastpayCB;
    private final CreditCardProviderServiceFakeImpl fake = new CreditCardProviderServiceFakeImpl();

    public CreditCardProviderServiceFastpayImpl(FastpayCreditCardAPIClient fastpayCreditCardAPIClient, CircuitBreakerFactory fastpayCB) {
        this.fastpayCreditCardAPIClient = fastpayCreditCardAPIClient;
        this.fastpayCB = fastpayCB.create(FAST_API_CB);
    }

    @Override
    @ConcurrencyLimit(2)
    public LimitedCreditCard register(UUID customerId, String tokenizedCard) {
        final var input = FastpayCreditCardInput.builder()
                .tokenizedCard(tokenizedCard)
                .customerCode(customerId.toString())
                .build();

        try {
            var response = fastpayCB.run(
                    () -> fastpayCreditCardAPIClient.create(input),
                    ex -> {
                        log.error("fail call faspay, details {}", ex.getCause());
                        var result = fake.register(customerId, tokenizedCard);

                        return new FastpayCreditCardResponse(
                                result.getGatewayCode(),
                                result.getLastNumbers(),
                                result.getExpMonth(),
                                result.getExpYear(),
                                result.getBrand());
                    });

            return toLimitedCreditCard(response);
        } catch (NoFallbackAvailableException e) {
            log.error("failt process payment, cause {}", e.getCause());
            throw new BadGatewayException("fail request algapay", e.getCause());
        }
    }

    @Override
    public Optional<LimitedCreditCard> findById(String gatewayCode) {
        FastpayCreditCardResponse response;

        try {
            response = fastpayCreditCardAPIClient.findById(gatewayCode);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }

        return Optional.ofNullable(toLimitedCreditCard(response));
    }

    @Override
    public void delete(String gatewayCode) {
        fastpayCreditCardAPIClient.delete(gatewayCode);
    }

    private static LimitedCreditCard toLimitedCreditCard(FastpayCreditCardResponse response) {
        return LimitedCreditCard.builder()
                .brand(response.getBrand())
                .lastNumbers(response.getLastNumbers())
                .expYear(response.getExpYear())
                .expMonth(response.getExpMonth())
                .gatewayCode(response.getId())
                .build();
    }
}
