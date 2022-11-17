// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.CurrencyResolution;

public final class CurrencyResolutionPropertiesHelper {
    private static CurrencyResolutionAccessor accessor;

    private CurrencyResolutionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CurrencyResolution} instance.
     */
    public interface CurrencyResolutionAccessor {
        void setIso4217(CurrencyResolution currencyResolution, String iso4217);
        void setUnit(CurrencyResolution currencyResolution, String unit);
        void setValue(CurrencyResolution currencyResolution, double value);
    }

    /**
     * The method called from {@link CurrencyResolution} to set it's accessor.
     *
     * @param currencyResolutionAccessor The accessor.
     */
    public static void setAccessor(final CurrencyResolutionAccessor currencyResolutionAccessor) {
        accessor = currencyResolutionAccessor;
    }

    public static void setISO4217(CurrencyResolution currencyResolution, String iso4217) {
        accessor.setIso4217(currencyResolution, iso4217);
    }

    public static void setUnit(CurrencyResolution currencyResolution, String unit) {
        accessor.setUnit(currencyResolution, unit);
    }

    public static void setValue(CurrencyResolution currencyResolution, double value) {
        accessor.setValue(currencyResolution, value);
    }
}
