// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import java.util.Collection;

import com.azure.core.util.ExpandableStringEnum;

/**
 * Defines the requirement type for feature flag filter conditions.
 */
public final class FeatureFlagRequirementType extends ExpandableStringEnum<FeatureFlagRequirementType> {
    /**
     * Any filter must match.
     */
    public static final FeatureFlagRequirementType ANY = fromString("Any");

    /**
     * All filters must match.
     */
    public static final FeatureFlagRequirementType ALL = fromString("All");

    /**
     * Creates a new instance of FeatureFlagRequirementType value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public FeatureFlagRequirementType() {
    }

    /**
     * Creates or finds a FeatureFlagRequirementType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding FeatureFlagRequirementType.
     */
    public static FeatureFlagRequirementType fromString(String name) {
        return fromString(name, FeatureFlagRequirementType.class);
    }

    /**
     * Gets known FeatureFlagRequirementType values.
     *
     * @return known FeatureFlagRequirementType values.
     */
    public static Collection<FeatureFlagRequirementType> values() {
        return values(FeatureFlagRequirementType.class);
    }
}
