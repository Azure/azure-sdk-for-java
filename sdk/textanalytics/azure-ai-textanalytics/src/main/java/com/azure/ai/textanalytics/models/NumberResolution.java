// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.NumberResolutionPropertiesHelper;
import com.azure.core.annotation.Immutable;

/** A resolution for numeric entity instances. */
@Immutable
public final class NumberResolution extends BaseResolution {
    /*
     * The type of the extracted number entity.
     */
    private NumberKind numberKind;

    /*
     * A numeric representation of what the extracted text denotes.
     */
    private double value;

    static {
        NumberResolutionPropertiesHelper.setAccessor(
            new NumberResolutionPropertiesHelper.NumberResolutionAccessor() {
                @Override
                public void setNumberKind(NumberResolution numberResolution, NumberKind numberKind) {
                    numberResolution.setNumberKind(numberKind);
                }

                @Override
                public void setValue(NumberResolution numberResolution, double value) {
                    numberResolution.setValue(value);
                }
            });
    }

    /**
     * Get the numberKind property: The type of the extracted number entity.
     *
     * @return the numberKind value.
     */
    public NumberKind getNumberKind() {
        return this.numberKind;
    }

    /**
     * Get the value property: A numeric representation of what the extracted text denotes.
     *
     * @return the value value.
     */
    public double getValue() {
        return this.value;
    }

    @Override
    public ResolutionKind getType() {
        return ResolutionKind.NUMBER_RESOLUTION;
    }

    private void setNumberKind(NumberKind numberKind) {
        this.numberKind = numberKind;
    }

    private void setValue(double value) {
        this.value = value;
    }
}
