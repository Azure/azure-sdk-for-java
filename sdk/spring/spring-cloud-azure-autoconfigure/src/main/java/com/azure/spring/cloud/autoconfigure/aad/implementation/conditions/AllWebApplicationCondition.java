// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.conditions;

import com.azure.spring.cloud.autoconfigure.aad.properties.AADApplicationType;

import static com.azure.spring.cloud.autoconfigure.aad.properties.AADApplicationType.RESOURCE_SERVER;
import static com.azure.spring.cloud.autoconfigure.aad.properties.AADApplicationType.RESOURCE_SERVER_WITH_OBO;

/**
 * Web application or all in scenario condition.
 */
public final class AllWebApplicationCondition extends AbstractApplicationTypeCondition {

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
