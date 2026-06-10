package com.microsoft.agentserver.sample.financial;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.Locale;
import java.util.Map;

public class ExchangeTool {
    private static final Map<String, Double> exchangeRates = Map.of(
        "USD_EUR", 0.9,
        "USD_GBP", 0.8,
        "EUR_USD", 1.1,
        "EUR_GBP", 0.88,
        "GBP_USD", 1.25,
        "GBP_EUR", 1.14
    );

    @Tool("Exchange the given amount of money from the original to the target currency")
    Double exchange(@P("originalCurrency") String originalCurrency, @P("amount") Double amount, @P("targetCurrency") String targetCurrency) {
        if (originalCurrency.toUpperCase(Locale.ROOT).equals(targetCurrency.toUpperCase(Locale.ROOT))) {
            return amount;
        }

        String key = originalCurrency.toUpperCase(Locale.ROOT) + "_" + targetCurrency.toUpperCase(Locale.ROOT);

        if (!exchangeRates.containsKey(key)) {
            throw new RuntimeException("No exchange rate found for " + key);
        }

        return amount * exchangeRates.get(key);
    }
}
