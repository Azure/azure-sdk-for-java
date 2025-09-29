// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation.targeting;

import java.util.ArrayList;
import java.util.List;

/**
 * Deny list of a TargetingFilter rollout
 */
public class Exclusion {

    private List<String> users = new ArrayList<>();

    private List<String> groups = new ArrayList<>();

    /**
     * @return the users
     */
    public List<String> getUsers() {
        return users;
    }

    /**
     * @param users the users to set
     * @return the updated Exclusion object
     */
    public Exclusion setUsers(List<String> users) {
        this.users = users;
        return this;
    }

    /**
     * @return the groups
     */
    public List<String> getGroups() {
        return groups;
    }

    /**
     * @param groups the audiences to set
     * @return the updated Exclusion object
     */
    public Exclusion setGroups(List<String> groups) {
        this.groups = groups;
        return this;
    }

}
