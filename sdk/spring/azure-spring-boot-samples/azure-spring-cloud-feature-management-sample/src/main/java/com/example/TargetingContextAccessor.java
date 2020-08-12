/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.example;

import java.util.ArrayList;

import com.microsoft.azure.spring.cloud.feature.manager.targeting.ITargetingContextAccessor;
import com.microsoft.azure.spring.cloud.feature.manager.targeting.TargetingContext;

import reactor.core.publisher.Mono;

public class TargetingContextAccessor implements ITargetingContextAccessor {

    @Override
    public Mono<TargetingContext> getContextAsync() {
        TargetingContext context = new TargetingContext();
        context.setUserId("Jeff");
        ArrayList<String> groups = new ArrayList<String>();
        groups.add("Ring0");
        context.setGroups(groups);
        return Mono.just(context);
    }

}
