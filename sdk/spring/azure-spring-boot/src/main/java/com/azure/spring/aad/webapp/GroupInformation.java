// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import java.util.HashSet;
import java.util.Set;

/**
 * Contains information about the group.
 */
public class GroupInformation {

    private Set<String> groupsId = new HashSet<>();

    private Set<String> groupsName = new HashSet<>();

    public Set<String> getGroupsId() {
        return groupsId;
    }

    public void setGroupsId(Set<String> groupsId) {
        this.groupsId = groupsId;
    }

    public Set<String> getGroupsName() {
        return groupsName;
    }

    public void setGroupsName(Set<String> groupsName) {
        this.groupsName = groupsName;
    }
}
