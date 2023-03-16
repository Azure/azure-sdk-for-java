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
    private String iso4217;

    /*
     * The unit of the amount captured in the extracted entity
     */
    private String unit;

    /*
     * The numeric value that the extracted text denotes.
     */
    private double value;

    static {
        CurrencyResolutionPropertiesHelper.setAccessor(
            new CurrencyResolutionPropertiesHelper.CurrencyResolutionAccessor() {
                @Override
                public void setIso4217(CurrencyResolution currencyResolution, String iso4217) {
                    currencyResolution.setIso4217(iso4217);
                }

                @Override
                public void setUnit(CurrencyResolution currencyResolution, String unit) {
                    currencyResolution.setUnit(unit);
                }

                @Override
                public void setValue(CurrencyResolution currencyResolution, double value) {
                    currencyResolution.setValue(value);
                }
            });
    }

    /**
     * Get the ISO4217 property: The alphabetic code based on another ISO standard, ISO 3166, which lists the codes for
     * country names. The first two letters of the ISO 4217 three-letter code are the same as the code for the country
     * name, and, where possible, the third letter corresponds to the first letter of the currency name.
     *
     * @return the iso4217 value.
     */
    public String getIso4217() {
        return this.iso4217;
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

    @Override
    public ResolutionKind getType() {
        return ResolutionKind.CURRENCY_RESOLUTION;
    }

    private void setIso4217(String iso4217) {
        this.iso4217 = iso4217;
    }

    private void setUnit(String unit) {
        this.unit = unit;
    }

    private void setValue(double value) {
        this.value = value;
    }
}
