/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.compute.v2020_12_01;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for PatchOperationStatus.
 */
public final class PatchOperationStatus extends ExpandableStringEnum<PatchOperationStatus> {
    /** Static value Unknown for PatchOperationStatus. */
    public static final PatchOperationStatus UNKNOWN = fromString("Unknown");

    /** Static value InProgress for PatchOperationStatus. */
    public static final PatchOperationStatus IN_PROGRESS = fromString("InProgress");

    /** Static value Failed for PatchOperationStatus. */
    public static final PatchOperationStatus FAILED = fromString("Failed");

    /** Static value Succeeded for PatchOperationStatus. */
    public static final PatchOperationStatus SUCCEEDED = fromString("Succeeded");

    /** Static value CompletedWithWarnings for PatchOperationStatus. */
    public static final PatchOperationStatus COMPLETED_WITH_WARNINGS = fromString("CompletedWithWarnings");

    /**
     * Creates or finds a PatchOperationStatus from its string representation.
     * @param name a name to look for
     * @return the corresponding PatchOperationStatus
     */
    @JsonCreator
    public static PatchOperationStatus fromString(String name) {
        return fromString(name, PatchOperationStatus.class);
    }

    /**
     * @return known PatchOperationStatus values
     */
    public static Collection<PatchOperationStatus> values() {
        return values(PatchOperationStatus.class);
    }
}
