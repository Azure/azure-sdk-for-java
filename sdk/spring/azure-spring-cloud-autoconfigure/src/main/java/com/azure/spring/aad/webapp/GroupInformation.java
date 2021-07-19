// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import java.util.HashSet;
import java.util.Set;

/**
 * Contains information about the group.
 */
public class GroupInformation {

    private Set<String> groupsIds = new HashSet<>();

    private Set<String> groupsNames = new HashSet<>();

    public Set<String> getGroupsIds() {
        return groupsIds;
    }

    public void setGroupsIds(Set<String> groupsIds) {
        this.groupsIds = groupsIds;
    }

    public Set<String> getGroupsNames() {
        return groupsNames;
    }

    public void setGroupsNames(Set<String> groupsNames) {
        this.groupsNames = groupsNames;
    }
}
