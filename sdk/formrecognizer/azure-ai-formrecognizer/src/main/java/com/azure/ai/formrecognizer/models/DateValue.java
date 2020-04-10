// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import java.time.LocalDate;

/**
 * Class to represent the Date value for
 * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueDate()}
 */
public class DateValue extends FieldValue<LocalDate> {

    /*
     * Date value.
     */
    private final LocalDate valueDate;

    /*
     * Type of the FieldValue.
     */
    private final FieldValueType fieldValueType;

    /**
     * Constructs a DateValue.
     *
     * @param valueDate Date value.
     */
    public DateValue(LocalDate valueDate) {
        this.valueDate = valueDate;
        this.fieldValueType = FieldValueType.DATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDate getValue() {
        return this.valueDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldValueType getType() {
        return this.fieldValueType;
    }

}
