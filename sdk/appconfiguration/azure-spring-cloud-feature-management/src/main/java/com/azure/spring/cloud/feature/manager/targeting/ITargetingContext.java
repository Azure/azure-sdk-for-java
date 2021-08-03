// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.targeting;

import java.util.List;

public interface ITargetingContext {

    /**
     * @return the userId
     */
    String getUserId();

    /**
     * @param userId the userId to set
     */
    void setUserId(String userId);

    /**
     * @return the groups
     */
    List<String> getGroups();

    /**
     * @param groups the groups to set
     */
    void setGroups(List<String> groups);

}
