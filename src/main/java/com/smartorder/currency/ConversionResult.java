package com.smartorder.currency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionResult {

    private double originalAmount;
    private String originalCurrency;
    private double convertedAmount;
    private String convertedCurrency;
    private double conversionRate;
}