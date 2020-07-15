/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager.targeting;

import java.util.List;

public interface ITargetingContext {

    /**
     * @return the userId
     */
    public String getUserId();

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId);

    /**
     * @return the groups
     */
    public List<String> getGroups();

    /**
     * @param groups the groups to set
     */
    public void setGroups(List<String> groups);

}
