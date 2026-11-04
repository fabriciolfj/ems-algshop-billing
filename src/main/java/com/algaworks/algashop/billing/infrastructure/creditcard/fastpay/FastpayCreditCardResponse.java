package com.algaworks.algashop.billing.infrastructure.creditcard.fastpay;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FastpayCreditCardResponse {

    private String id;
    private String lastNumbers;
    private Integer expMonth;
    private Integer expYear;
    private String brand;
}
