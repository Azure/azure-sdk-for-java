// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Controls how multiple feature filters are combined.
 */
public final class RequirementType extends ExpandableStringEnum<RequirementType> {
    /** At least one filter must be satisfied. */
    public static final RequirementType ANY = fromString("Any");

    /** All filters must be satisfied. */
    public static final RequirementType ALL = fromString("All");

    /**
     * Creates a requirement type.
     * @deprecated Use {@link #fromString(String)} instead.
     */
    @Deprecated
    public RequirementType() {
    }

    /**
     * Creates or finds a requirement type from its wire value.
     * @param name The wire value.
     * @return The requirement type.
     */
    public static RequirementType fromString(String name) {
        return fromString(name, RequirementType.class);
    }

    /**
     * Gets the known requirement types.
     * @return The known requirement types.
     */
    public static Collection<RequirementType> values() {
        return values(RequirementType.class);
    }
}
