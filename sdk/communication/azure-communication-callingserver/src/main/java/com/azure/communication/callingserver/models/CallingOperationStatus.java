// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for CallingOperationStatus. */
public final class CallingOperationStatus extends ExpandableStringEnum<CallingOperationStatus> {
    /** Static value notStarted for CallingOperationStatus. */
    public static final CallingOperationStatus NOT_STARTED = fromString("notStarted");

    /** Static value running for CallingOperationStatus. */
    public static final CallingOperationStatus RUNNING = fromString("running");

    /** Static value completed for CallingOperationStatus. */
    public static final CallingOperationStatus COMPLETED = fromString("completed");

    /** Static value failed for CallingOperationStatus. */
    public static final CallingOperationStatus FAILED = fromString("failed");

    /**
     * Creates or finds a CallingOperationStatus from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding CallingOperationStatus.
     */
    public static CallingOperationStatus fromString(String name) {
        return fromString(name, CallingOperationStatus.class);
    }

    /** @return known CallingOperationStatus values. */
    public static Collection<CallingOperationStatus> values() {
        return values(CallingOperationStatus.class);
    }
}
