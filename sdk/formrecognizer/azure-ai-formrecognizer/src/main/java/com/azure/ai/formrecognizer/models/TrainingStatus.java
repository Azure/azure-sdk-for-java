// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.ExpandableStringEnum;

/**
 * Define enum values for TrainingStatus.
 */
public final class TrainingStatus extends ExpandableStringEnum<TrainingStatus> {

    /**
     * Static value succeeded for TrainingStatus.
     */
    public static final TrainingStatus SUCCEEDED = fromString("succeeded");
    /**
     * Static value partiallySucceeded for TrainingStatus.
     */
    public static final TrainingStatus PARTIALLY_SUCCEEDED = fromString("partiallySucceeded");

    /**
     * Static value failed for TrainingStatus.
     */
    public static final TrainingStatus FAILED = fromString("failed");

    /**
     * Parses a serialized value to a {@link TrainingStatus} instance.
     *
     * @param value the serialized value to parse.
     *
     * @return the parsed TrainingStatus object, or null if unable to parse.
     */
    public static TrainingStatus fromString(String value) {
        return fromString(value, TrainingStatus.class);
    }

}
