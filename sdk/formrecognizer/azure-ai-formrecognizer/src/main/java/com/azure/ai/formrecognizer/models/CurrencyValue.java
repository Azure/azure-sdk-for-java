// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.CurrencyValueHelper;

/**
 * Currency field value.
 */
public final class CurrencyValue {
    /*
     * Currency amount.
     */
    private double amount;

    /*
     * Currency symbol label, if any.
     */
    private String symbol;

    /**
     * Get the amount property: Currency amount.
     *
     * @return the amount value.
     */
    public double getAmount() {
        return this.amount;
    }

    /**
     * Set the amount property: Currency amount.
     *
     * @param amount the amount value to set.
     */
    void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Get the currencySymbol property: Currency symbol label, if any.
     *
     * @return the currencySymbol value.
     */
    public String getSymbol() {
        return this.symbol;
    }

    /**
     * Set the currencySymbol property: Currency symbol label, if any.
     *
     * @param symbol the currencySymbol value to set.
     */
    void setSymbol(String symbol) {
        this.symbol = symbol;
    }


    static {
        CurrencyValueHelper.setAccessor(new CurrencyValueHelper.CurrencyValueAccessor() {

            @Override
            public void setAmount(CurrencyValue currencyValue, double amount) {
                currencyValue.setAmount(amount);
            }

            @Override
            public void setSymbol(CurrencyValue currencyValue, String currencySymbol) {
                currencyValue.setSymbol(currencySymbol);
            }
        });
    }
}
