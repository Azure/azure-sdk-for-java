// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.CurrencyValueHelper;
import com.azure.core.annotation.Immutable;

/**
 * Currency field value.
 */
@Immutable
public final class CurrencyValue {
    /*
     * Currency amount.
     */
    private double amount;

    /*
     * Currency symbol label, if any.
     */
    private String symbol;
    private String code;

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
    private void setAmount(double amount) {
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
    private void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Get the resolved currency code (ISO 4217), if any.
     *
     * @return the code value.
     */
    public String getCode() {
        return code;
    }

    private void setCode(String code) {
        this.code = code;
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

            @Override
            public void setCode(CurrencyValue currencyValue, String code) {
                currencyValue.setCode(code);
            }
        });
    }
}
