// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import java.util.List;

/**
 * Defines how to allocate variants based on context, including percentile-based, user-based, and group-based
 * allocation.
 */
public final class FeatureFlagAllocation {
    private String defaultWhenDisabled;
    private String defaultWhenEnabled;
    private List<FeatureFlagPercentileAllocation> percentile;
    private List<FeatureFlagUserAllocation> user;
    private List<FeatureFlagGroupAllocation> group;
    private String seed;

    /**
     * Creates an instance of FeatureFlagAllocation.
     */
    public FeatureFlagAllocation() {
    }

    /**
     * Gets the default variant to use when the flag is disabled.
     *
     * @return the default variant when disabled.
     */
    public String getDefaultWhenDisabled() {
        return this.defaultWhenDisabled;
    }

    /**
     * Sets the default variant to use when the flag is disabled.
     *
     * @param defaultWhenDisabled the default variant when disabled.
     * @return the updated FeatureFlagAllocation object.
     */
    public FeatureFlagAllocation setDefaultWhenDisabled(String defaultWhenDisabled) {
        this.defaultWhenDisabled = defaultWhenDisabled;
        return this;
    }

    /**
     * Gets the default variant to use when the flag is enabled but not allocated.
     *
     * @return the default variant when enabled.
     */
    public String getDefaultWhenEnabled() {
        return this.defaultWhenEnabled;
    }

    /**
     * Sets the default variant to use when the flag is enabled but not allocated.
     *
     * @param defaultWhenEnabled the default variant when enabled.
     * @return the updated FeatureFlagAllocation object.
     */
    public FeatureFlagAllocation setDefaultWhenEnabled(String defaultWhenEnabled) {
        this.defaultWhenEnabled = defaultWhenEnabled;
        return this;
    }

    /**
     * Gets the percentile-based allocations.
     *
     * @return the percentile allocations.
     */
    public List<FeatureFlagPercentileAllocation> getPercentile() {
        return this.percentile;
    }

    /**
     * Sets the percentile-based allocations.
     *
     * @param percentile the percentile allocations.
     * @return the updated FeatureFlagAllocation object.
     */
    public FeatureFlagAllocation setPercentile(List<FeatureFlagPercentileAllocation> percentile) {
        this.percentile = percentile;
        return this;
    }

    /**
     * Gets the user-based allocations.
     *
     * @return the user allocations.
     */
    public List<FeatureFlagUserAllocation> getUser() {
        return this.user;
    }

    /**
     * Sets the user-based allocations.
     *
     * @param user the user allocations.
     * @return the updated FeatureFlagAllocation object.
     */
    public FeatureFlagAllocation setUser(List<FeatureFlagUserAllocation> user) {
        this.user = user;
        return this;
    }

    /**
     * Gets the group-based allocations.
     *
     * @return the group allocations.
     */
    public List<FeatureFlagGroupAllocation> getGroup() {
        return this.group;
    }

    /**
     * Sets the group-based allocations.
     *
     * @param group the group allocations.
     * @return the updated FeatureFlagAllocation object.
     */
    public FeatureFlagAllocation setGroup(List<FeatureFlagGroupAllocation> group) {
        this.group = group;
        return this;
    }

    /**
     * Gets the seed used for random allocation.
     *
     * @return the seed.
     */
    public String getSeed() {
        return this.seed;
    }

    /**
     * Sets the seed used for random allocation.
     *
     * @param seed the seed.
     * @return the updated FeatureFlagAllocation object.
     */
    public FeatureFlagAllocation setSeed(String seed) {
        this.seed = seed;
        return this;
    }
}
