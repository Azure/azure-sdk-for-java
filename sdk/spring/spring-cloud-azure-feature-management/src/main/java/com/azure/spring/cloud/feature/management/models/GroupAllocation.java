// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.models;

import java.util.List;

/**
 * Group allocation of a variant for feature flag targeting. This class defines how specific
 * user groups are assigned to a particular variant of a feature. It contains a reference
 * to a variant name and a list of group identifiers that should receive this variant
 * when the feature flag is evaluated.
 */
public class GroupAllocation {

    /**
     * Creates a new instance of the GroupAllocation class.
     */
    public GroupAllocation() {
    }

    /**
     * The name of the variant that will be assigned to the specified groups.
     * This corresponds to a variant defined in the feature flag configuration.
     */
    private String variant;

    /**
     * The list of group identifiers that should receive this variant.
     * When a user belongs to any of these groups, they will be assigned
     * this variant during feature flag evaluation.
     */
    private List<String> groups;

    /**
     * Gets the name of the variant that is assigned to the specified groups.
     * 
     * @return the variant name for this group allocation
     */
    public String getVariant() {
        return variant;
    }

    /**
     * Sets the name of the variant that should be assigned to the specified groups.
     * This should match a valid variant name defined in the feature flag configuration.
     * 
     * @param variant the variant name to assign to the groups
     * @return the updated GroupAllocation object
     */
    public GroupAllocation setVariant(String variant) {
        this.variant = variant;
        return this;
    }

    /**
     * Gets the list of group identifiers that should receive this variant.
     * When a user belongs to any of these groups, they will be assigned
     * this variant during feature flag evaluation.
     * 
     * @return the list of group identifiers for this allocation
     */
    public List<String> getGroups() {
        return groups;
    }

    /**
     * Sets the list of group identifiers that should receive this variant.
     * When a user belongs to any of these groups, they will be assigned
     * this variant during feature flag evaluation.
     * 
     * @param groups the list of group identifiers to associate with this variant
     * @return the updated GroupAllocation object
     */
    public GroupAllocation setGroups(List<String> groups) {
        this.groups = groups;
        return this;
    }

}
