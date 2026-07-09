package com.algaworks.algashop.billing.infrastructure.payment.fastpay;

import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardNotFoundException;
import com.algaworks.algashop.billing.domain.model.creditcard.CreditCardRepository;
import com.algaworks.algashop.billing.domain.model.invoice.payment.Payment;
import com.algaworks.algashop.billing.domain.model.invoice.payment.PaymentGatewayService;
import com.algaworks.algashop.billing.domain.model.invoice.payment.PaymentRequest;
import com.algaworks.algashop.billing.infrastructure.payment.AlgaShopPaymentProperties;
import com.algaworks.algashop.billing.presentation.BadGatewayException;
import com.algaworks.algashop.billing.presentation.GatewayTimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "algashop.integrations.payment.provider", havingValue = "FASTPAY")
public class PaymentGatewayServiceFastpayImpl implements PaymentGatewayService {

    private final FastpayPaymentApiClient fastpayPaymentAPIClient;
    private final CreditCardRepository creditCardRepository;
    private final AlgaShopPaymentProperties algaShopPaymentProperties;


    @Override
    public Payment capture(PaymentRequest request) {
        FastpayPaymentInput input = convertInput(request);
        FastpayPaymentModel response;

        try {
            response = fastpayPaymentAPIClient.capture(input);
        } catch (ResourceAccessException e) {
            throw new GatewayTimeoutException("Fastpay API Timeout", e);
        } catch (HttpClientErrorException e) {
            throw new BadGatewayException("Fastpay API Bad Gateway", e);
        }

        return convertToPayment(response);
    }

    @Override
    public Payment findByCode(String gatewayCode) {
        FastpayPaymentModel response;
        try {
            response = fastpayPaymentAPIClient.findById(gatewayCode);
        } catch (ResourceAccessException e) {
            throw new GatewayTimeoutException("Fastpay API Timeout", e);
        } catch (HttpClientErrorException e) {
            throw new BadGatewayException("Fastpay API Bad Gateway", e);
        }

        return convertToPayment(response);
    }

    private Payment convertToPayment(FastpayPaymentModel response) {
        var builder = Payment.builder()
                .gatewayCode(response.getId())
                .invoiceId(UUID.fromString(response.getReferenceCode()));

        FastpayPaymentMethod fastpayPaymentMethod;
        try {
            fastpayPaymentMethod = FastpayPaymentMethod.valueOf(response.getMethod());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown payment method: " + response.getMethod());
        }

        FastpayPaymentStatus fastpayPaymentStatus;
        try {
            fastpayPaymentStatus = FastpayPaymentStatus.valueOf(response.getStatus());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown payment status: " + response.getStatus());
        }

        builder.method(FastpayEnumConverter.convert(fastpayPaymentMethod));
        builder.status(FastpayEnumConverter.convert(fastpayPaymentStatus));
        return builder.build();
    }

    private FastpayPaymentInput convertInput(PaymentRequest request) {
        var payer = request.getPayer();
        var address = payer.getAddress();

        var builder = FastpayPaymentInput.builder()
                .totalAmount(request.getAmount())
                .referenceCode(request.getInvoiceId().toString())
                .fullName(payer.getFullName())
                .document(payer.getDocument())
                .phone(payer.getPhone())
                .zipCode(address.getZipCode())
                .addressLine1(address.getStreet() + ", " + address.getNumber())
                .addressLine2(address.getComplement())
                .replyToUrl(algaShopPaymentProperties.getFastpay().getWebhookUrl());

        switch (request.getMethod()) {
            case CREDIT_CARD -> {
                builder.method(FastpayPaymentMethod.CREDIT.name());
                var creditCard = creditCardRepository.findById(request.getCreditCardId())
                        .orElseThrow(CreditCardNotFoundException::new);
                builder.creditCardId(creditCard.getGatewayCode());
            }
            case GATEWAY_BALANCE -> {
                builder.method(FastpayPaymentMethod.GATEWAY_BALANCE.name());
            }
        }
        return builder.build();
    }
}
