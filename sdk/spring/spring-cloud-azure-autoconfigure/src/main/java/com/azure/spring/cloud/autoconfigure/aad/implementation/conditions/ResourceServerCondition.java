// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.conditions;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadApplicationType;
import org.springframework.context.annotation.Condition;

import static com.azure.spring.cloud.autoconfigure.aad.properties.AadApplicationType.RESOURCE_SERVER;
import static com.azure.spring.cloud.autoconfigure.aad.properties.AadApplicationType.RESOURCE_SERVER_WITH_OBO;
import static com.azure.spring.cloud.autoconfigure.aad.properties.AadApplicationType.WEB_APPLICATION_AND_RESOURCE_SERVER;

/**
 * {@link Condition} that checks for resource server scenario.
 */
public final class ResourceServerCondition extends AbstractApplicationTypeCondition {

    @Override
    boolean isTargetApplicationType(AadApplicationType applicationType) {
        return applicationType == RESOURCE_SERVER || applicationType == RESOURCE_SERVER_WITH_OBO
            || applicationType == WEB_APPLICATION_AND_RESOURCE_SERVER;
    }

    @Override
    protected String getConditionTitle() {
        return "AAD Resource Server Condition";
    }
}
