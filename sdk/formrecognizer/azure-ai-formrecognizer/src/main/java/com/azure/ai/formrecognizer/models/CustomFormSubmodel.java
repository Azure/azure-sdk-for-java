// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

import java.util.Map;

/**
 * The CustomFormSubmodel model.
 */
@Immutable
public final class CustomFormSubmodel {

    /*
     * Estimated extraction accuracy for this model.
     */
    private final Float accuracy;

    /*
     * Map of fields used to train the model.
     */
    private final Map<String, CustomFormModelField> fieldMap;

    /*
     * The form type.
     */
    private final String formType;

    /**
     * Constructs a CustomFormSubmodel object.
     *
     * @param accuracy The estimated extraction accuracy for this model.
     * @param fieldMap The Map of fields used to train the model.
     * @param formType The recognized form type.
     */
    public CustomFormSubmodel(final Float accuracy, final Map<String, CustomFormModelField> fieldMap,
        final String formType) {
        this.accuracy = accuracy;
        this.fieldMap = fieldMap;
        this.formType = formType;
    }

    /**
     * Get the estimated extraction accuracy for this model.
     *
     * @return the accuracy value.
     */
    public Float getAccuracy() {
        return this.accuracy;
    }

    /**
     * Gets the recognized form type for the model.
     *
     * @return the form type for the model.
     */
    public String getFormType() {
        return this.formType;
    }

    /**
     * Gets the extracted fields map.
     *
     * @return The extracted fields map.
     */
    public Map<String, CustomFormModelField> getFieldMap() {
        return this.fieldMap;
    }
}
