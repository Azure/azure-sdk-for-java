// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.graph;

import java.util.HashSet;
import java.util.Set;

/**
 * Contains information about the group.
 */
public class GroupInformation {

    private Set<String> groupsIds = new HashSet<>();

    private Set<String> groupsNames = new HashSet<>();

    /**
     * Gets the set of group IDs.
     *
     * @return the set of group IDs
     */
    public Set<String> getGroupsIds() {
        return groupsIds;
    }

    /**
     * Sets the set of group IDs.
     *
     * @param groupsIds the set of group IDs
     */
    public void setGroupsIds(Set<String> groupsIds) {
        this.groupsIds = groupsIds;
    }

    /**
     * Gets the set of group names.
     *
     * @return the set of group names
     */
    public Set<String> getGroupsNames() {
        return groupsNames;
    }

    /**
     * Sets the set of group names.
     *
     * @param groupsNames the set of group names
     */
    public void setGroupsNames(Set<String> groupsNames) {
        this.groupsNames = groupsNames;
    }
}
