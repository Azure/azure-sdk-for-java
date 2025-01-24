// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
     * @param users the users to set
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
     * @param groups the groups to set
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
     */
    public Allocation setSeed(String seed) {
        this.seed = seed;
        return this;
    }

}
