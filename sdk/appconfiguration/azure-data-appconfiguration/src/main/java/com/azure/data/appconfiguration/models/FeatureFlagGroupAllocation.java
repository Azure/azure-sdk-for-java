// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import java.util.List;

/**
 * Allocates specific groups to a variant.
 */
public final class FeatureFlagGroupAllocation {
    private final String variant;
    private final List<String> groups;

    /**
     * Creates an instance of FeatureFlagGroupAllocation.
     *
     * @param variant the variant to allocate these groups to.
     * @param groups the groups to get this variant.
     */
    public FeatureFlagGroupAllocation(String variant, List<String> groups) {
        this.variant = variant;
        this.groups = groups;
    }

    /**
     * Gets the variant to allocate these groups to.
     *
     * @return the variant name.
     */
    public String getVariant() {
        return this.variant;
    }

    /**
     * Gets the groups to get this variant.
     *
     * @return the groups.
     */
    public List<String> getGroups() {
        return this.groups;
    }
}
