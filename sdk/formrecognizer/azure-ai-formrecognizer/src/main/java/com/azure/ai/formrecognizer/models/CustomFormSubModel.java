// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

import java.util.Map;

/**
 * The CustomFormSubModel model.
 */
@Immutable
public class CustomFormSubModel {

    /*
     * Estimated extraction accuracy for this model.
     */
    private final float accuracy;

    /*
     * List of fields used to train the model and the train operation error
     * reported by each.
     */
    private final Map<String, CustomFormModelField> fieldMap;

    /**
     * The form type.
     */
    private final String formType;

    public CustomFormSubModel(final Float accuracy, final Map<String, CustomFormModelField> fieldMap, 
        final String formType) {
        this.accuracy = accuracy;
        this.fieldMap = fieldMap;
        this.formType = formType;
    }

    /**
     * Get the accuracy property: Estimated extraction accuracy for this model.
     *
     * @return the accuracy value.
     */
    public float getAccuracy() {
        return this.accuracy;
    }

    /**
     * Gets the form type for the model.
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
