package com.smartorder.currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class CurrencyRateScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(CurrencyRateScheduler.class);

    @Autowired
    private CurrencyRateService currencyRateService;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    public void updateCurrencyRates() {
        log.info("=== Scheduled job: Updating currency rates ===");
        currencyRateService.fetchAndSaveRates();
        log.info("=== Currency rates update complete ===");
    }

    // Run once on startup to initialize rates
    @Scheduled(initialDelay = 5000, fixedDelay = Long.MAX_VALUE)
    public void initializeCurrencyRates() {
        log.info("=== Initializing currency rates on startup ===");
        currencyRateService.fetchAndSaveRates();
        log.info("=== Currency rates initialized ===");
    }
}