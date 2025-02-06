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

    @JsonProperty("default_when_enabled")
    private String defaultWhenEnabled;

    @JsonProperty("default_when_disabled")
    private String defaultWhenDisabled;

    private List<UserAllocation> user = new ArrayList<>();

    private List<GroupAllocation> group = new ArrayList<>();

    private List<PercentileAllocation> percentile = new ArrayList<>();

    private String seed;

    /**
     * @return the defaultWhenEnabled
     */
    public String getDefaultWhenEnabled() {
        return defaultWhenEnabled;
    }

    /**
     * @param defaultWhenEnabled the defaultWhenEnabled to set
     * @return Allocation
     */
    public Allocation setDefaultWhenEnabled(String defaultWhenEnabled) {
        this.defaultWhenEnabled = defaultWhenEnabled;
        return this;
    }

    /**
     * @return the defaultWhenDisabled
     */
    public String getDefaultWhenDisabled() {
        return defaultWhenDisabled;
    }

    /**
     * @param defaultWhenDisabled the defaultWhenDisabled to set
     * @return Allocation
     */
    public Allocation setDefaultWhenDisabled(String defaultWhenDisabled) {
        this.defaultWhenDisabled = defaultWhenDisabled;
        return this;
    }

    /**
     * @return the users
     */
    public List<UserAllocation> getUser() {
        return user;
    }

    /**
     * @param user the users to set
     * @return Allocation
     */
    public Allocation setUser(List<UserAllocation> user) {
        this.user = user;
        return this;
    }

    /**
     * @return the groups
     */
    public List<GroupAllocation> getGroup() {
        return group;
    }

    /**
     * @param group the groups to set
     * @return Allocation
     */
    public Allocation setGroups(List<GroupAllocation> group) {
        this.group = group;
        return this;
    }

    /**
     * @return the percentile
     */
    public List<PercentileAllocation> getPercentile() {
        return percentile;
    }

    /**
     * @param percentile the percentile to set
     * @return Allocation
     */
    public Allocation setPercentile(List<PercentileAllocation> percentile) {
        this.percentile = percentile;
        return this;
    }

    /**
     * @return the seed
     */
    public String getSeed() {
        return seed;
    }

    /**
     * @param seed the seed to set
     * @return Allocation
     */
    public Allocation setSeed(String seed) {
        this.seed = seed;
        return this;
    }

}
