package com.algaworks.algashop.billing.application.creditcard.management;

import com.algaworks.algashop.billing.domain.model.creditcard.CreditCard;
import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardNotFoundException;
import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardProviderService;
import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditCardManagementService {

    private CreditCardRepository creditCardRepository;
    private CreditCardProviderService creditCardProviderService;

    @Transactional
    public UUID register(TokenizedCreditCardInput input) {
        var limitedCreditCard = creditCardProviderService.register(input.getCustomerId(), input.getTokenizedCard());

        var creditCard = CreditCard.brandNew(
                input.getCustomerId(),
                limitedCreditCard.getLastNumbers(),
                limitedCreditCard.getBrand(),
                limitedCreditCard.getExpMonth(),
                limitedCreditCard.getExpYear(),
                limitedCreditCard.getGatewayCode()
        );

        creditCardRepository.saveAndFlush(creditCard);

        return creditCard.getId();
    }

    @Transactional
    public void delete(UUID customerId, UUID creditCardId) {
        var creditCard = creditCardRepository.findByCustomerIdAndId(customerId, creditCardId)
                .orElseThrow(CreditCardNotFoundException::new);

        creditCardRepository.delete(creditCard);
        creditCardProviderService.delete(creditCard.getGatewayCode());
    }
}
