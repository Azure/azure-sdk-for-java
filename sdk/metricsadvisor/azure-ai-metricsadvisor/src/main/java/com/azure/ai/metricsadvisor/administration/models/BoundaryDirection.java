// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Describes the direction of boundary used in anomaly boundary conditions.
 */
public final class BoundaryDirection extends ExpandableStringEnum<BoundaryDirection> {
    /**
     * Defines the lower boundary in a boundary condition.
     */
    public static final  BoundaryDirection LOWER = fromString("LOWER");
    /**
     * Defines the upper boundary in a boundary condition.
     */
    public static final  BoundaryDirection UPPER = fromString("UPPER");
    /**
     * Defines both lower and upper boundary in a boundary condition.
     */
    public static final  BoundaryDirection BOTH = fromString("BOTH");

    /**
     * Creates or finds a BoundaryDirection from its string representation.
     *
     * @param name a name to look for.
     *
     * @return the corresponding BoundaryDirection.
     */
    public static BoundaryDirection fromString(String name) {
        return fromString(name, BoundaryDirection.class);
    }

    /**
     * @return known BoundaryDirection values.
     */
    public static Collection<BoundaryDirection> values() {
        return values(BoundaryDirection.class);
    }
}
