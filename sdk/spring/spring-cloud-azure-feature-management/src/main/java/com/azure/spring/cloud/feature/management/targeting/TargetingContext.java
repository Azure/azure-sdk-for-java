// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.targeting;

import java.util.List;

/**
 * Interface for the Targeting Context used in evaluating the `Microsoft.TargetingFilter`.
 */
public interface TargetingContext {

    /**
     * Return the userId
     * @return the userId
     */
    String getUserId();

    /**
     * Set the userId
     * @param userId the userId to set
     */
    void setUserId(String userId);

    /**
     * Return the groups
     * @return the groups
     */
    List<String> getGroups();

    /**
     * Set the groups
     * @param groups the groups to set
     */
    void setGroups(List<String> groups);

}
