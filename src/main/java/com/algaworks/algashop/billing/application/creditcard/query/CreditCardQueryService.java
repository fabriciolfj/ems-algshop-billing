package com.algaworks.algashop.billing.application.creditcard.query;


import java.util.List;
import java.util.UUID;

public interface CreditCardQueryService {

    CreditCardOutput findOne(final UUID customerId, final UUID creditCardId);
    List<CreditCardOutput> findByCustomer(final UUID customerId);
}
