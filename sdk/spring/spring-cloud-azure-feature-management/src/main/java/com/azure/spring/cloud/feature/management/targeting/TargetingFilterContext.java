// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.targeting;

import java.util.List;

/**
 * Context for evaluating the `Microsoft.TargetingFilter`.
 */
public final class TargetingFilterContext implements TargetingContext {

    /**
     * Creates an instance of {@link TargetingFilterContext}
     */
    public TargetingFilterContext() {
    }

    private String userId;

    private List<String> groups;

    /**
     * @return the userId
     */
    @Override
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the groups
     */
    @Override
    public List<String> getGroups() {
        return groups;
    }

    /**
     * @param groups the groups to set
     */
    @Override
    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

}
