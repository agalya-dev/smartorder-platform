package com.smartorder.currency;

import com.couchbase.client.java.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CurrencyConverter {

    private static final Logger log =
            LoggerFactory.getLogger(CurrencyConverter.class);

    private static final String BASE_CURRENCY = "USD";
    private static final String TARGET_CURRENCY = "SEK";

    @Autowired
    private CurrencyRateService currencyRateService;

    // Convert any amount from any currency to SEK
    public ConversionResult convertToSEK(
            double amount, String fromCurrency) {

        log.info("Converting {} {} to SEK", amount, fromCurrency);

        try {
            // If already SEK — no conversion needed
            if (TARGET_CURRENCY.equalsIgnoreCase(fromCurrency)) {
                return ConversionResult.builder()
                        .originalAmount(amount)
                        .originalCurrency(fromCurrency)
                        .convertedAmount(amount)
                        .convertedCurrency(TARGET_CURRENCY)
                        .conversionRate(1.0)
                        .build();
            }

            // Get rates from CB
            JsonObject doc = currencyRateService.getRates();
            if (doc == null) {
                log.warn("No currency rates in CB — " +
                        "returning original amount");
                return ConversionResult.builder()
                        .originalAmount(amount)
                        .originalCurrency(fromCurrency)
                        .convertedAmount(amount)
                        .convertedCurrency(fromCurrency)
                        .conversionRate(1.0)
                        .build();
            }

            JsonObject rates = doc.getObject("rates");

            // Get rate for source currency
            Double fromRate = rates.getDouble(
                    fromCurrency.toUpperCase());
            // Get rate for SEK
            Double sekRate = rates.getDouble(TARGET_CURRENCY);

            if (fromRate == null || sekRate == null) {
                log.warn("Rate not found for currency: {}",
                        fromCurrency);
                return ConversionResult.builder()
                        .originalAmount(amount)
                        .originalCurrency(fromCurrency)
                        .convertedAmount(amount)
                        .convertedCurrency(fromCurrency)
                        .conversionRate(1.0)
                        .build();
            }

            // Convert: fromCurrency → USD → SEK
            double amountInUSD = amount / fromRate;
            double amountInSEK = amountInUSD * sekRate;
            double conversionRate = sekRate / fromRate;

            // Round to 2 decimal places
            amountInSEK = Math.round(amountInSEK * 100.0) / 100.0;
            conversionRate = Math.round(
                    conversionRate * 100000.0) / 100000.0;

            log.info("Converted {} {} to {} SEK (rate: {})",
                    amount, fromCurrency, amountInSEK, conversionRate);

            return ConversionResult.builder()
                    .originalAmount(amount)
                    .originalCurrency(fromCurrency.toUpperCase())
                    .convertedAmount(amountInSEK)
                    .convertedCurrency(TARGET_CURRENCY)
                    .conversionRate(conversionRate)
                    .build();

        } catch (Exception e) {
            log.error("Currency conversion failed: {}",
                    e.getMessage());
            return ConversionResult.builder()
                    .originalAmount(amount)
                    .originalCurrency(fromCurrency)
                    .convertedAmount(amount)
                    .convertedCurrency(fromCurrency)
                    .conversionRate(1.0)
                    .build();
        }
    }
}