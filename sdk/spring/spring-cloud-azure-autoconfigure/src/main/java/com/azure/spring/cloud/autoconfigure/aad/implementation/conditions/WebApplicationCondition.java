// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.conditions;

import com.azure.spring.cloud.autoconfigure.aad.properties.AADApplicationType;
import org.springframework.context.annotation.Condition;

import static com.azure.spring.cloud.autoconfigure.aad.properties.AADApplicationType.RESOURCE_SERVER;
import static com.azure.spring.cloud.autoconfigure.aad.properties.AADApplicationType.RESOURCE_SERVER_WITH_OBO;

/**
 * {@link Condition} that checks for Web application scenario.
 */
public final class WebApplicationCondition extends AbstractApplicationTypeCondition {

    @Override
    boolean isNonTargetApplicationType(AADApplicationType applicationType) {
        return applicationType == null || applicationType == RESOURCE_SERVER
            || applicationType == RESOURCE_SERVER_WITH_OBO;
    }

    @Override
    protected String getConditionTitle() {
        return "AAD Web Application Condition";
    }
}
