// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.models;

import java.util.List;

/**
 * User allocation of a variant for feature flag targeting. This class defines how specific
 * users are assigned to a particular variant of a feature. It contains a reference to
 * a variant name and a list of user identifiers that should receive this variant
 * when the feature flag is evaluated. This enables targeted feature rollout to specific users.
 */
public class UserAllocation {

    /**
     * Creates a new instance of the UserAllocation class.
     */
    public UserAllocation() {
    }

    /**
     * The name of the variant that will be assigned to the specified users.
     * This corresponds to a variant defined in the feature flag configuration.
     */
    private String variant;

    /**
     * The list of user identifiers that should receive this variant.
     * When a user matches any of these identifiers, they will be assigned
     * this variant during feature flag evaluation.
     */
    private List<String> users;

    /**
     * Gets the name of the variant that is assigned to the specified users.
     * 
     * @return the variant name for this user allocation
     */
    public String getVariant() {
        return variant;
    }

    /**
     * Sets the name of the variant that should be assigned to the specified users.
     * This should match a valid variant name defined in the feature flag configuration.
     * 
     * @param variant the variant name to assign to the users
     * @return the updated UserAllocation object
     */
    public UserAllocation setVariant(String variant) {
        this.variant = variant;
        return this;
    }

    /**
     * Gets the list of user identifiers that should receive this variant.
     * These identifiers are typically usernames, email addresses, or user IDs
     * that uniquely identify users in the system.
     * 
     * @return the list of user identifiers for this allocation
     */
    public List<String> getUsers() {
        return users;
    }

    /**
     * Sets the list of user identifiers that should receive this variant.
     * When a user matches any of these identifiers, they will be assigned
     * this variant during feature flag evaluation.
     * 
     * @param users the list of user identifiers to associate with this variant
     * @return the updated UserAllocation object
     */
    public UserAllocation setUsers(List<String> users) {
        this.users = users;
        return this;
    }

}
