// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for OperationStatus. */
@Immutable
public final class OperationStatus extends ExpandableStringEnum<OperationStatus> {
    /** Static value notStarted for OperationStatus. */
    public static final OperationStatus NOT_STARTED = fromString("notStarted");

    /** Static value running for OperationStatus. */
    public static final OperationStatus RUNNING = fromString("running");

    /** Static value failed for OperationStatus. */
    public static final OperationStatus FAILED = fromString("failed");

    /** Static value succeeded for OperationStatus. */
    public static final OperationStatus SUCCEEDED = fromString("succeeded");

    /** Static value canceled for OperationStatus. */
    public static final OperationStatus CANCELED = fromString("canceled");

    /**
     * Creates or finds a OperationStatus from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding OperationStatus.
     */
    public static OperationStatus fromString(String name) {
        return fromString(name, OperationStatus.class);
    }

    /** @return known OperationStatus values. */
    public static Collection<OperationStatus> values() {
        return values(OperationStatus.class);
    }
}
