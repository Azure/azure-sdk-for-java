// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines enum values for ModelTrainingStatus.
 */
public final class ModelTrainingStatus extends ExpandableStringEnum<ModelTrainingStatus> {

    /**
     * Static value Creating for ModelTrainingStatus.
     */
    public static final ModelTrainingStatus CREATING = fromString("creating");

    /**
     * Static value Creating for ModelTrainingStatus.
     */
    public static final ModelTrainingStatus READY = fromString("ready");

    /**
     * Static value Creating for ModelTrainingStatus.
     */
    public static final ModelTrainingStatus INVALID = fromString("invalid");

    /**
     * Parses a serialized value to a {@code ModelTrainingStatus} instance.
     *
     * @param value the serialized value to parse.
     *
     * @return the parsed ModelTrainingStatus object, or null if unable to parse.
     */
    public static ModelTrainingStatus fromString(String value) {
        return fromString(value, ModelTrainingStatus.class);
    }

    /**
     * @return known {@link ModelTrainingStatus} values.
     */
    public static Collection<ModelTrainingStatus> values() {
        return values(ModelTrainingStatus.class);
    }
}
