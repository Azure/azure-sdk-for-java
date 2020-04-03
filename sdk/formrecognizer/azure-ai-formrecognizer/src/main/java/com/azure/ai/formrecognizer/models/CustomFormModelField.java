// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * The CustomFormModelField model.
 */
@Immutable
public class CustomFormModelField {

    /*
     * Training field name.
     */
    private final String fieldText;

    /*
     * Estimated extraction accuracy for this field.
     */
    private final Float accuracy;

    /**
     * Constructs a CustomFormModelField object.
     *
     * @param fieldText Training field name.
     * @param accuracy Estimated extraction accuracy for this field.
     */
    public CustomFormModelField(final String fieldText, final Float accuracy) {
        this.fieldText = fieldText;
        this.accuracy = accuracy;
    }

    /**
     * Get the field text property: Training field name.
     *
     * @return the fieldName value.
     */
    public String getFieldText() {
        return this.fieldText;
    }

    /**
     * Get the accuracy property: Estimated extraction accuracy for this field.
     *
     * @return the accuracy value.
     */
    public Float getAccuracy() {
        return this.accuracy;
    }
}
