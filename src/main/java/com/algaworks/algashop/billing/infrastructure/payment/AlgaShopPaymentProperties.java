package com.algaworks.algashop.billing.infrastructure.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Data
@Validated
@ConfigurationProperties(prefix = "algashop.integrations.payment")
public class AlgaShopPaymentProperties {

    @NotNull
    private AlgaShopPaymentProvider provider;

    @NotNull
    private FastpayProperties fastpay;

    public enum AlgaShopPaymentProvider {
        FAKE,
        FASTPAY
    }

    @Validated
    @Data
    public static class FastpayProperties {
        @NotBlank
        private String hostname;

        @NotBlank
        private String privateToken;

        @NotBlank
        private String webhookUrl;

    }
}
