// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation.models;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Allocation {

    private String defaultWhenEnabled;

    private String defaultWhenDisabled;

    private Map<String, UserAllocation> users = new HashMap<>();

    private Map<String, GroupAllocation> groups = new HashMap<>();

    private Map<String, PercentileAllocation> percentile = new HashMap<>();

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
    public Map<String, UserAllocation> getUsers() {
        return users;
    }

    /**
     * @param users the users to set
     */
    public Allocation setUsers(Map<String, UserAllocation> users) {
        this.users = users;
        return this;
    }

    /**
     * @return the groups
     */
    public Map<String, GroupAllocation> getGroups() {
        return groups;
    }

    /**
     * @param groups the groups to set
     */
    public Allocation setGroups(Map<String, GroupAllocation> groups) {
        this.groups = groups;
        return this;
    }

    /**
     * @return the percentile
     */
    public Map<String, PercentileAllocation> getPercentile() {
        return percentile;
    }

    /**
     * @param percentile the percentile to set
     */
    public Allocation setPercentile(Map<String, PercentileAllocation> percentile) {
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
