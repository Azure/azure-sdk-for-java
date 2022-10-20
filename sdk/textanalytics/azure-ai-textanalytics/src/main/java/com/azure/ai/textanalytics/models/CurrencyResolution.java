// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.CurrencyResolutionPropertiesHelper;
import com.azure.core.annotation.Immutable;

/** Represents the currency entity resolution model. */
@Immutable
public final class CurrencyResolution extends BaseResolution {
    /*
     * The alphabetic code based on another ISO standard, ISO 3166, which lists the codes for country names. The first
     * two letters of the ISO 4217 three-letter code are the same as the code for the country name, and, where
     * possible, the third letter corresponds to the first letter of the currency name.
     */
    private String iSO4217;

    /*
     * The unit of the amount captured in the extracted entity
     */
    private final String unit;

    /*
     * The numeric value that the extracted text denotes.
     */
    private final double value;

    /**
     * Create a currency entity resolution model.
     *
     * @param unit The unit of the amount captured in the extracted entity.
     * @param value The numeric value that the extracted text denotes.
     */
    public CurrencyResolution(String unit, double value) {
        this.unit = unit;
        this.value = value;
    }

    static {
        CurrencyResolutionPropertiesHelper.setAccessor(
                (currencyResolution, iso4217) -> currencyResolution.setISO4217(iso4217));
    }

    /**
     * Get the iSO4217 property: The alphabetic code based on another ISO standard, ISO 3166, which lists the codes for
     * country names. The first two letters of the ISO 4217 three-letter code are the same as the code for the country
     * name, and, where possible, the third letter corresponds to the first letter of the currency name.
     *
     * @return the iSO4217 value.
     */
    public String getISO4217() {
        return this.iSO4217;
    }

    /**
     * Set the iSO4217 property: The alphabetic code based on another ISO standard, ISO 3166, which lists the codes for
     * country names. The first two letters of the ISO 4217 three-letter code are the same as the code for the country
     * name, and, where possible, the third letter corresponds to the first letter of the currency name.
     *
     * @param iSO4217 the iSO4217 value to set.
     * @return the CurrencyResolution object itself.
     */
    private void setISO4217(String iSO4217) {
        this.iSO4217 = iSO4217;
    }

    /**
     * Get the unit property: The unit of the amount captured in the extracted entity.
     *
     * @return the unit value.
     */
    public String getUnit() {
        return this.unit;
    }

    /**
     * Get the value property: The numeric value that the extracted text denotes.
     *
     * @return the value value.
     */
    public double getValue() {
        return this.value;
    }
}
