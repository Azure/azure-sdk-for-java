// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation.targeting;

import java.util.List;

/**
 * Deny list of a TargetingFilter rollout
 */
public class Exclusion {

    private List<String> users;

    private List<String> groups;

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

    /**
     * @return the groups
     */
    public List<String> getGroups() {
        return groups;
    }

    /**
     * @param groups the audiences to set
     */
    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

}
