// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for SingleBoundaryDirection.
 */
public final class SingleBoundaryDirection extends ExpandableStringEnum<SingleBoundaryDirection> {
    /**
     * Defines the lower boundary in a boundary condition.
     */
    public static final  SingleBoundaryDirection LOWER = fromString("LOWER");
    /**
     * Defines the upper boundary in a boundary condition.
     */
    public static final  SingleBoundaryDirection UPPER = fromString("UPPER");

    /**
     * Creates or finds a BoundaryDirection from its string representation.
     *
     * @param name a name to look for.
     *
     * @return the corresponding BoundaryDirection.
     */
    public static SingleBoundaryDirection fromString(String name) {
        return fromString(name, SingleBoundaryDirection.class);
    }

    /**
     * @return known BoundaryDirection values.
     */
    public static Collection<SingleBoundaryDirection> values() {
        return values(SingleBoundaryDirection.class);
    }
}
