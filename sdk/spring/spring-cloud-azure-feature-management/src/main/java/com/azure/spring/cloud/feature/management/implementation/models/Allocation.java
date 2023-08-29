// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Allocation {

    @JsonProperty("default-when-enabled")
    private String defaultWhenEnabled;

    @JsonProperty("default-when-disabled")
    private String defautlWhenDisabled;

    private List<VariantAssignmentUsers> users;

    private List<VariantAssignmentGroups> groups;

    private List<Percentile> percentile;

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
    public void setDefaultWhenEnabled(String defaultWhenEnabled) {
        this.defaultWhenEnabled = defaultWhenEnabled;
    }

    /**
     * @return the defautlWhenDisabled
     */
    public String getDefautlWhenDisabled() {
        return defautlWhenDisabled;
    }

    /**
     * @param defautlWhenDisabled the defautlWhenDisabled to set
     */
    public void setDefautlWhenDisabled(String defautlWhenDisabled) {
        this.defautlWhenDisabled = defautlWhenDisabled;
    }

    /**
     * @return the users
     */
    public List<VariantAssignmentUsers> getUsers() {
        return users;
    }

    /**
     * @param users the users to set
     */
    public void setUsers(List<VariantAssignmentUsers> users) {
        this.users = users;
    }

    /**
     * @return the groups
     */
    public List<VariantAssignmentGroups> getGroups() {
        return groups;
    }

    /**
     * @param groups the groups to set
     */
    public void setGroups(List<VariantAssignmentGroups> groups) {
        this.groups = groups;
    }

    /**
     * @return the percentile
     */
    public List<Percentile> getPercentile() {
        return percentile;
    }

    /**
     * @param percentile the percentile to set
     */
    public void setPercentile(List<Percentile> percentile) {
        this.percentile = percentile;
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
    public void setSeed(String seed) {
        this.seed = seed;
    }

}
