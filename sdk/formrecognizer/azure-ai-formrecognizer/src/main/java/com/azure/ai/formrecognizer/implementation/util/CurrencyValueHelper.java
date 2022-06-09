// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.CurrencyValue;

/**
 * The helper class to set the non-public properties of an {@link CurrencyValue} instance.
 */
public final class CurrencyValueHelper {
    private static CurrencyValueAccessor accessor;

    private CurrencyValueHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link CurrencyValue} instance.
     */
    public interface CurrencyValueAccessor {
        void setAmount(CurrencyValue currencyValue, double amount);
        void setSymbol(CurrencyValue currencyValue, String currencySymbol);
    }

    /**
     * The method called from {@link CurrencyValue} to set it's accessor.
     *
     * @param currencyValueAccessor The accessor.
     */
    public static void setAccessor(final CurrencyValueAccessor currencyValueAccessor) {
        accessor = currencyValueAccessor;
    }

    static void setAmount(CurrencyValue currencyValue, double amount) {
        accessor.setAmount(currencyValue, amount);
    }

    static void setCurrencySymbol(CurrencyValue currencyValue, String currencySymbol) {
        accessor.setSymbol(currencyValue, currencySymbol);
    }
}
