package com.skybooker.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.payment")
public class PaymentProperties {

    private String provider;
    private String currency;
    private boolean mockMode;
    private String receiptBaseUrl;
    private final Razorpay razorpay = new Razorpay();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isMockMode() {
        return mockMode;
    }

    public void setMockMode(boolean mockMode) {
        this.mockMode = mockMode;
    }

    public String getReceiptBaseUrl() {
        return receiptBaseUrl;
    }

    public void setReceiptBaseUrl(String receiptBaseUrl) {
        this.receiptBaseUrl = receiptBaseUrl;
    }

    public Razorpay getRazorpay() {
        return razorpay;
    }

    public static class Razorpay {
        private String keyId;
        private String keySecret;
        private String webhookSecret;

        public String getKeyId() {
            return keyId;
        }

        public void setKeyId(String keyId) {
            this.keyId = keyId;
        }

        public String getKeySecret() {
            return keySecret;
        }

        public void setKeySecret(String keySecret) {
            this.keySecret = keySecret;
        }

        public String getWebhookSecret() {
            return webhookSecret;
        }

        public void setWebhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
        }
    }
}
