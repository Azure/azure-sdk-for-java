// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.sample.financial.openai;

import java.util.Locale;
import java.util.Map;

/**
 * A tiny fixed-rate currency converter used by the sample's {@code exchange}
 * tool. Carries no framework annotations — see {@link FinancialToolbox} for the
 * tool schema and dispatch.
 */
public class ExchangeTool {

    private static final Map<String, Double> EXCHANGE_RATES = Map.of(
        "USD_EUR", 0.9,
        "USD_GBP", 0.8,
        "EUR_USD", 1.1,
        "EUR_GBP", 0.88,
        "GBP_USD", 1.25,
        "GBP_EUR", 1.14
    );

    /**
     * Converts an amount from one currency to another using fixed demo rates.
     *
     * @param originalCurrency the source currency code (for example {@code USD})
     * @param amount           the amount in the source currency
     * @param targetCurrency   the target currency code (for example {@code EUR})
     * @return the converted amount
     */
    public double exchange(String originalCurrency, double amount, String targetCurrency) {
        String from = originalCurrency.toUpperCase(Locale.ROOT);
        String to = targetCurrency.toUpperCase(Locale.ROOT);
        if (from.equals(to)) {
            return amount;
        }
        String key = from + "_" + to;
        Double rate = EXCHANGE_RATES.get(key);
        if (rate == null) {
            throw new IllegalArgumentException("No exchange rate found for " + key);
        }
        return amount * rate;
    }
}
