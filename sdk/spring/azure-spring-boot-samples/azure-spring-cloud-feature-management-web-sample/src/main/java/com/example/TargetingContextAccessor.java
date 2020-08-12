/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.example;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.microsoft.azure.spring.cloud.feature.manager.targeting.ITargetingContextAccessor;
import com.microsoft.azure.spring.cloud.feature.manager.targeting.TargetingContext;

import reactor.core.publisher.Mono;

public class TargetingContextAccessor implements ITargetingContextAccessor {

    @Autowired
    private HttpServletRequest requestContext;

    @Override
    public Mono<TargetingContext> getContextAsync() {
        TargetingContext context = new TargetingContext();
        context.setUserId(requestContext.getParameter("User"));
        String group = requestContext.getParameter("Group");
        if (StringUtils.hasText(group)) {
            List<String> groups = Arrays.asList(group.split("\\s*,\\s*"));
            context.setGroups(groups);
        }
        return Mono.just(context);
    }

}
