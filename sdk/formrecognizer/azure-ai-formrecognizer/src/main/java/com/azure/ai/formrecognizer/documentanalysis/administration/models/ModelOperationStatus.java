// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for ModelOperationStatus. */
public final class ModelOperationStatus extends ExpandableStringEnum<ModelOperationStatus> {
    /** Static value notStarted for ModelOperationStatus. */
    public static final ModelOperationStatus NOT_STARTED = fromString("notStarted");

    /** Static value running for ModelOperationStatus. */
    public static final ModelOperationStatus RUNNING = fromString("running");

    /** Static value failed for ModelOperationStatus. */
    public static final ModelOperationStatus FAILED = fromString("failed");

    /** Static value succeeded for ModelOperationStatus. */
    public static final ModelOperationStatus SUCCEEDED = fromString("succeeded");

    /** Static value canceled for ModelOperationStatus. */
    public static final ModelOperationStatus CANCELED = fromString("canceled");

    /**
     * Creates or finds a ModelOperationStatus from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ModelOperationStatus.
     */
    public static ModelOperationStatus fromString(String name) {
        return fromString(name, ModelOperationStatus.class);
    }

    /** @return known ModelOperationStatus values. */
    public static Collection<ModelOperationStatus> values() {
        return values(ModelOperationStatus.class);
    }
}
