// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import java.util.List;

/**
 * Allocates specific users to a variant.
 */
public final class FeatureFlagUserAllocation {
    private final String variant;
    private final List<String> users;

    /**
     * Creates an instance of FeatureFlagUserAllocation.
     *
     * @param variant the variant to allocate these users to.
     * @param users the users to get this variant.
     */
    public FeatureFlagUserAllocation(String variant, List<String> users) {
        this.variant = variant;
        this.users = users;
    }

    /**
     * Gets the variant to allocate these users to.
     *
     * @return the variant name.
     */
    public String getVariant() {
        return this.variant;
    }

    /**
     * Gets the users to get this variant.
     *
     * @return the users.
     */
    public List<String> getUsers() {
        return this.users;
    }
}
