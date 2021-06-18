// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.targeting;

import java.util.List;

public class Audience {

    private List<String> users;

    private List<GroupRollout> groups;

    private double defaultRolloutPercentage;

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
    public List<GroupRollout> getGroups() {
        return groups;
    }

    /**
     * @param groups the audiences to set
     */
    public void setGroups(List<GroupRollout> groups) {
        this.groups = groups;
    }

    /**
     * @return the defaultRolloutPercentage
     */
    public double getDefaultRolloutPercentage() {
        return defaultRolloutPercentage;
    }

    /**
     * @param defaultRolloutPercentage the defaultRolloutPercentage to set
     */
    public void setDefaultRolloutPercentage(double defaultRolloutPercentage) {
        this.defaultRolloutPercentage = defaultRolloutPercentage;
    }

}
