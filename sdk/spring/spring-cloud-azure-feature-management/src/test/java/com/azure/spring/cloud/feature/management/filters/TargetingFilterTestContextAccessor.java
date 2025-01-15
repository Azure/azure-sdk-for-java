// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.filters;

import com.azure.spring.cloud.feature.management.targeting.TargetingContext;
import com.azure.spring.cloud.feature.management.targeting.TargetingContextAccessor;

import java.util.List;

public class TargetingFilterTestContextAccessor implements TargetingContextAccessor {

    private String user;

    private List<String> groups;

    public TargetingFilterTestContextAccessor(String user, List<String> groups) {
        this.user = user;
        this.groups = groups;
    }

    @Override
    public void configureTargetingContext(TargetingContext context) {
        context.setUserId(user);
        context.setGroups(groups);
    }

}
