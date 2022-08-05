// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for ModelOperationStatus.
 */
public final class ModelOperationStatus extends ExpandableStringEnum<ModelOperationStatus> {

    /**
     * Enum value notStarted.
     */
    public static final ModelOperationStatus NOT_STARTED = fromString("notStarted");
    /**
     * Enum value running.
     */
    public static final ModelOperationStatus RUNNING = fromString("running");
    /**
     * Enum value failed.
     */
    public static final ModelOperationStatus FAILED = fromString("failed");
    /**
     * Enum value succeeded.
     */
    public static final ModelOperationStatus SUCCEEDED = fromString("succeeded");
    /**
     * Enum value canceled.
     */
    public static final ModelOperationStatus CANCELED = fromString("canceled");

    /**
     * The actual serialized value for a ModelOperationStatus instance.
     */
    private final String value;

    ModelOperationStatus(String value) {
        this.value = value;
    }

    /**
     * Creates or finds a ModelOperationStatus from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ModelOperationStatus.
     */
    public static ModelOperationStatus fromString(String name) {
        return fromString(name, ModelOperationStatus.class);
    }

    /**
     * @return known ModelOperationStatus values.
     */
    public static Collection<ModelOperationStatus> values() {
        return values(ModelOperationStatus.class);
    }

}
