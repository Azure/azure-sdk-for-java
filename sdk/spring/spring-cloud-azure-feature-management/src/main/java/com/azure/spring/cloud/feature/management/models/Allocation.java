// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Allocation of a feature flag to variants.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Allocation {

    /**
     * Creates a new instance of the Allocation class.
     */
    public Allocation() {
    }

    @JsonProperty("default_when_enabled")
    private String defaultWhenEnabled;

    @JsonProperty("default_when_disabled")
    private String defaultWhenDisabled;

    private List<UserAllocation> user = new ArrayList<>();

    private List<GroupAllocation> group = new ArrayList<>();

    private List<PercentileAllocation> percentile = new ArrayList<>();

    private String seed;

    /**
     * Gets the variant to use when the feature flag is enabled and no specific allocation matches.
     *
     * @return the default variant when the feature flag is enabled
     */
    public String getDefaultWhenEnabled() {
        return defaultWhenEnabled;
    }

    /**
     * Sets the variant to use when the feature flag is enabled and no specific allocation matches.
     *
     * @param defaultWhenEnabled the default variant when enabled
     * @return the updated Allocation object
     */
    public Allocation setDefaultWhenEnabled(String defaultWhenEnabled) {
        this.defaultWhenEnabled = defaultWhenEnabled;
        return this;
    }

    /**
     * Gets the variant to use when the feature flag is disabled and no specific allocation matches.
     *
     * @return the default variant when the feature flag is disabled
     */
    public String getDefaultWhenDisabled() {
        return defaultWhenDisabled;
    }

    /**
     * Sets the variant to use when the feature flag is disabled and no specific allocation matches.
     *
     * @param defaultWhenDisabled the default variant when disabled
     * @return the updated Allocation object
     */
    public Allocation setDefaultWhenDisabled(String defaultWhenDisabled) {
        this.defaultWhenDisabled = defaultWhenDisabled;
        return this;
    }

    /**
     * Gets the list of user-specific allocations for the feature flag.
     *
     * @return the list of user allocations
     */
    public List<UserAllocation> getUser() {
        return user;
    }

    /**
     * Sets the list of user-specific allocations for the feature flag.
     *
     * @param user the list of user allocations
     * @return the updated Allocation object
     */
    public Allocation setUser(List<UserAllocation> user) {
        this.user = user;
        return this;
    }

    /**
     * Gets the list of group-specific allocations for the feature flag.
     *
     * @return the list of group allocations
     */
    public List<GroupAllocation> getGroup() {
        return group;
    }

    /**
     * Sets the list of group-specific allocations for the feature flag.
     *
     * @param group the list of group allocations
     * @return the updated Allocation object
     */
    public Allocation setGroup(List<GroupAllocation> group) {
        this.group = group;
        return this;
    }

    /**
     * Gets the list of percentile-based allocations for the feature flag.
     *
     * @return the list of percentile allocations
     */
    public List<PercentileAllocation> getPercentile() {
        return percentile;
    }

    /**
     * Sets the list of percentile-based allocations for the feature flag.
     *
     * @param percentile the list of percentile allocations
     * @return the updated Allocation object
     */
    public Allocation setPercentile(List<PercentileAllocation> percentile) {
        this.percentile = percentile;
        return this;
    }

    /**
     * Gets the seed value used for randomization in allocation calculations.
     *
     * @return the seed value for allocation
     */
    public String getSeed() {
        return seed;
    }

    /**
     * Sets the seed value used for randomization in allocation calculations.
     *
     * @param seed the seed value for allocation
     * @return the updated Allocation object
     */
    public Allocation setSeed(String seed) {
        this.seed = seed;
        return this;
    }

}
