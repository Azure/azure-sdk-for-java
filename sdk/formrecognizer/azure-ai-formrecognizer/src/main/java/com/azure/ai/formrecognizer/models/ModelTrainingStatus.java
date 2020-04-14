// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

/**
 * A enum class represents all status a training model can have.
 */

@Immutable
public final class ModelTrainingStatus extends ExpandableStringEnum<ModelTrainingStatus> {
    /**
     * Enum value creating.
     */
    public static final ModelTrainingStatus CREATING = fromString("creating");

    /**
     * Enum value ready.
     */
    public static final ModelTrainingStatus READY = fromString("ready");


    /**
     * Enum value invalid.
     */
    public static final ModelTrainingStatus INVALID = fromString("invalid");

    /**
     * Parses a serialized value to a ModelStatus instance.
     *
     * @param value The serialized value to parse.
     * @return The parsed ModelStatus object, or null if unable to parse.
     */
    public static ModelTrainingStatus fromString(String value) {
        return fromString(value, ModelTrainingStatus.class);
    }
}
