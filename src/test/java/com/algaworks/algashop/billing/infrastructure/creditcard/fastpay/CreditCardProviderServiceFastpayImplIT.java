package com.algaworks.algashop.billing.infrastructure.creditcard.fastpay;

import com.algaworks.algashop.billing.infrastructure.AbstractFastpayIT;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class CreditCardProviderServiceFastpayImplIT extends AbstractFastpayIT {

    @Autowired
    private CreditCardProviderServiceFastpayImpl creditCardProvider;

    @Autowired
    private FastpayCreditCardTokenizationAPIClient tokenizationAPIClient;

    private static final UUID validCustomerId = UUID.randomUUID();
    private static final String alwaysPaidCardNumber = "4622943127011022";

    @BeforeAll
    public static void beforeAll() {
        startMock();
    }

    @AfterAll
    public static void afterAll() {
        stopMock();
    }

    @Test
    public void shouldRegisterCreditCard() {
        var limitedCreditCard = registerCard();

        Assertions.assertThat(limitedCreditCard.getGatewayCode()).isNotBlank();
    }

    @Test
    public void shouldFindRegisteredCreditCard() {
        var limitedCrediCard = registerCard();

        var limitedCrediCardFound = creditCardProvider.findById(limitedCrediCard.getGatewayCode()).orElseThrow();

        Assertions.assertThat(limitedCrediCardFound.getGatewayCode()).isEqualTo(limitedCrediCard.getGatewayCode());
    }

    @Test
    public void shouldDeleteRegisterCrediCard() {
        var limitedCrediCard = registerCard();

        creditCardProvider.delete(limitedCrediCard.getGatewayCode());
    }
}