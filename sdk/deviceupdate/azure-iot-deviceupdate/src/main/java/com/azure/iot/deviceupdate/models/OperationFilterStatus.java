// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.iot.deviceupdate.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for OperationFilterStatus. */
public final class OperationFilterStatus extends ExpandableStringEnum<OperationFilterStatus> {
    /** Static value Running for OperationFilterStatus. */
    public static final OperationFilterStatus RUNNING = fromString("Running");

    /** Static value NotStarted for OperationFilterStatus. */
    public static final OperationFilterStatus NOT_STARTED = fromString("NotStarted");

    /**
     * Creates or finds a OperationFilterStatus from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding OperationFilterStatus.
     */
    @JsonCreator
    public static OperationFilterStatus fromString(String name) {
        return fromString(name, OperationFilterStatus.class);
    }

    /** @return known OperationFilterStatus values. */
    public static Collection<OperationFilterStatus> values() {
        return values(OperationFilterStatus.class);
    }
}
