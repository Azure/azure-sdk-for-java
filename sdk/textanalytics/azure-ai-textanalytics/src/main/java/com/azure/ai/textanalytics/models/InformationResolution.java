// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.InformationResolutionPropertiesHelper;
import com.azure.core.annotation.Immutable;

/** Represents the information (data) entity resolution model. */
@Immutable
public final class InformationResolution extends BaseResolution {
    /*
     * The information (data) Unit of measurement.
     */
    private InformationUnit unit;

    /*
     * The numeric value that the extracted text denotes.
     */
    private double value;

    static {
        InformationResolutionPropertiesHelper.setAccessor(
            new InformationResolutionPropertiesHelper.InformationResolutionAccessor() {
                @Override
                public void setUnit(InformationResolution informationResolution, InformationUnit unit) {
                    informationResolution.setUnit(unit);
                }

                @Override
                public void setValue(InformationResolution informationResolution, double value) {
                    informationResolution.setValue(value);
                }
            });
    }

    /**
     * Get the unit property: The information (data) Unit of measurement.
     *
     * @return the unit value.
     */
    public InformationUnit getUnit() {
        return this.unit;
    }

    @Override
    public ResolutionKind getType() {
        return ResolutionKind.INFORMATION_RESOLUTION;
    }

    /**
     * Get the value property: The numeric value that the extracted text denotes.
     *
     * @return the value value.
     */
    public double getValue() {
        return this.value;
    }

    private void setUnit(InformationUnit unit) {
        this.unit = unit;
    }

    private void setValue(double value) {
        this.value = value;
    }
}
