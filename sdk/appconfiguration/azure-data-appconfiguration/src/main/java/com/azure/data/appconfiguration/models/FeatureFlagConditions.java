// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import java.util.List;

/**
 * The conditions that must be met for a feature flag to be enabled.
 */
public final class FeatureFlagConditions {
    private FeatureFlagRequirementType requirementType;
    private List<FeatureFlagFilter> filters;

    /**
     * Creates an instance of FeatureFlagConditions.
     */
    public FeatureFlagConditions() {
    }

    /**
     * Gets the requirement type for the conditions.
     *
     * @return the requirement type.
     */
    public FeatureFlagRequirementType getRequirementType() {
        return this.requirementType;
    }

    /**
     * Sets the requirement type for the conditions.
     *
     * @param requirementType the requirement type.
     * @return the updated FeatureFlagConditions object.
     */
    public FeatureFlagConditions setRequirementType(FeatureFlagRequirementType requirementType) {
        this.requirementType = requirementType;
        return this;
    }

    /**
     * Gets the filters that will conditionally enable or disable the flag.
     *
     * @return the filters.
     */
    public List<FeatureFlagFilter> getFilters() {
        return this.filters;
    }

    /**
     * Sets the filters that will conditionally enable or disable the flag.
     *
     * @param filters the filters.
     * @return the updated FeatureFlagConditions object.
     */
    public FeatureFlagConditions setFilters(List<FeatureFlagFilter> filters) {
        this.filters = filters;
        return this;
    }
}
