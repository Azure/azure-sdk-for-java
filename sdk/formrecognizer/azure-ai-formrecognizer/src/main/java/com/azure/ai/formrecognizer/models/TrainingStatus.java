// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/*
 * Define enum values for TrainingStatus.
 */
public final class TrainingStatus extends ExpandableStringEnum<TrainingStatus> {

    /**
     * Static value Creating for TrainingStatus.
     */
    public static final TrainingStatus SUCCEEDED = fromString("succeeded");
    /**
     * Static value Creating for TrainingStatus.
     */
    public static final TrainingStatus PARTIALLY_SUCCEEDED = fromString("partiallySucceeded");

    /**
     * Static value Creating for TrainingStatus.
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

    /**
     * @return known {@code TrainingStatus} values.
     */
    public static Collection<TrainingStatus> values() {
        return values(TrainingStatus.class);
    }


}
