package com.algaworks.algashop.billing.infrastructure.creditcard.fastpay;

import com.algaworks.algashop.billing.domain.model.creditcard.LimitedCreditCard;
import org.apache.tomcat.util.http.parser.TE;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Year;
import java.util.UUID;

@SpringBootTest
@Import(FastpayCreditCardTokenizationAPIClientConfig.class)
class CreditCardProviderServiceFastpayImplIT {

    @Autowired
    private CreditCardProviderServiceFastpayImpl creditCardProvider;

    @Autowired
    private FastpayCreditCardTokenizationAPIClient tokenizationAPIClient;

    private static final UUID validCustomerId = UUID.randomUUID();
    private static final String alwaysPaidCardNumber = "4622943127011022";

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

        var possibleCrediCard = creditCardProvider.findById(limitedCrediCard.getGatewayCode());

        Assertions.assertThat(possibleCrediCard).isEmpty();
    }

    private LimitedCreditCard registerCard() {
        var input = FastpayTokenizationInput.builder()
                .cvv("222")
                .number(alwaysPaidCardNumber)
                .expMonth(1)
                .holderName("test")
                .holderDocument("021212")
                .expYear(Year.now().getValue() + 5)
                .build();


        var response = tokenizationAPIClient.tokenize(input);

        var limitedCreditCard = creditCardProvider.register(validCustomerId, response.getTokenizedCard());
        return limitedCreditCard;
    }
}