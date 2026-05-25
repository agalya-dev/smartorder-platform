package com.smartorder.currency;

import com.couchbase.client.java.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;

@Service
public class CurrencyRateService {

    private static final Logger log =
            LoggerFactory.getLogger(CurrencyRateService.class);

    private static final String FRANKFURTER_URL =
            "https://api.frankfurter.dev/v1/latest?base=USD";

    @Autowired
    private CurrencyRateRepository currencyRateRepository;

    private final WebClient webClient =
            WebClient.builder().build();

    // Fetch rates from Frankfurter API and save to CB
    public void fetchAndSaveRates() {
        log.info("Fetching currency rates from Frankfurter API");
        try {
            String response = webClient
                    .get()
                    .uri(FRANKFURTER_URL)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null) {
                log.error("Empty response from Frankfurter API");
                return;
            }

            // Parse response
            JsonObject apiResponse = JsonObject.fromJson(response);
            JsonObject rates = apiResponse.getObject("rates");

            // Add USD itself as base
            rates.put("USD", 1.0);

            // Build CB document
            JsonObject doc = JsonObject.create()
                    .put("id", "CONFIG::CURRENCY_RATES")
                    .put("type", "CONFIG")
                    .put("baseCurrency", "USD")
                    .put("rates", rates)
                    .put("lastUpdated", LocalDate.now().toString())
                    .put("source", "Frankfurter API (ECB data)");

            // Save to Couchbase
            currencyRateRepository.save(doc);

            log.info("Currency rates updated successfully " +
                    "for date: {}", LocalDate.now());

        } catch (Exception e) {
            log.error("Failed to fetch currency rates: {}",
                    e.getMessage());
        }
    }

    // Get rates from CB
    public JsonObject getRates() {
        return currencyRateRepository.getRates();
    }
}