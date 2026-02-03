// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents variant allocation settings for a feature flag.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Allocation {

    @JsonProperty("default_when_disabled")
    private String defaultWhenDisabled;

    @JsonProperty("default_when_enabled")
    private String defaultWhenEnabled;

    @JsonProperty("user")
    private List<UserAllocation> user;

    @JsonProperty("group")
    private List<GroupAllocation> group;

    @JsonProperty("percentile")
    private List<PercentileAllocation> percentile;

    @JsonProperty("seed")
    private String seed;

    /**
     * Default constructor.
     */
    public Allocation() {
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
    public void setDefaultWhenDisabled(String defaultWhenDisabled) {
        this.defaultWhenDisabled = defaultWhenDisabled;
    }

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
     * @return the user
     */
    public List<UserAllocation> getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(List<UserAllocation> user) {
        this.user = user;
    }

    /**
     * @return the group
     */
    public List<GroupAllocation> getGroup() {
        return group;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(List<GroupAllocation> group) {
        this.group = group;
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
    public void setPercentile(List<PercentileAllocation> percentile) {
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

    /**
     * Represents user allocation settings.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class UserAllocation {

        @JsonProperty("variant")
        private String variant;

        @JsonProperty("users")
        private List<String> users;

        /**
         * Default constructor.
         */
        public UserAllocation() {
        }

        /**
         * @return the variant
         */
        public String getVariant() {
            return variant;
        }

        /**
         * @param variant the variant to set
         */
        public void setVariant(String variant) {
            this.variant = variant;
        }

        /**
         * @return the users
         */
        public List<String> getUsers() {
            return users;
        }

        /**
         * @param users the users to set
         */
        public void setUsers(List<String> users) {
            this.users = users;
        }
    }

    /**
     * Represents group allocation settings.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class GroupAllocation {

        @JsonProperty("variant")
        private String variant;

        @JsonProperty("groups")
        private List<String> groups;

        /**
         * Default constructor.
         */
        public GroupAllocation() {
        }

        /**
         * @return the variant
         */
        public String getVariant() {
            return variant;
        }

        /**
         * @param variant the variant to set
         */
        public void setVariant(String variant) {
            this.variant = variant;
        }

        /**
         * @return the groups
         */
        public List<String> getGroups() {
            return groups;
        }

        /**
         * @param groups the groups to set
         */
        public void setGroups(List<String> groups) {
            this.groups = groups;
        }
    }

    /**
     * Represents percentile allocation settings.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class PercentileAllocation {

        @JsonProperty("variant")
        private String variant;

        @JsonProperty("from")
        private Double from;

        @JsonProperty("to")
        private Double to;

        /**
         * Default constructor.
         */
        public PercentileAllocation() {
        }

        /**
         * @return the variant
         */
        public String getVariant() {
            return variant;
        }

        /**
         * @param variant the variant to set
         */
        public void setVariant(String variant) {
            this.variant = variant;
        }

        /**
         * @return the from
         */
        public Double getFrom() {
            return from;
        }

        /**
         * @param from the from to set
         */
        public void setFrom(Double from) {
            this.from = from;
        }

        /**
         * @return the to
         */
        public Double getTo() {
            return to;
        }

        /**
         * @param to the to to set
         */
        public void setTo(Double to) {
            this.to = to;
        }
    }
}
