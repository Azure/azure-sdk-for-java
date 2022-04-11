// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.conditions;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadApplicationType;
import org.springframework.context.annotation.Condition;

import static com.azure.spring.cloud.autoconfigure.aad.properties.AadApplicationType.RESOURCE_SERVER_WITH_OBO;

/**
 * {@link Condition} that checks for resource server with OBO scenario.
 */
public final class ResourceServerWithOBOCondition extends AbstractApplicationTypeCondition {

    @Override
    boolean isTargetApplicationType(AadApplicationType applicationType) {
        return applicationType == RESOURCE_SERVER_WITH_OBO;
    }

    @Override
    protected String getConditionTitle() {
        return "AAD Resource Server with OBO Condition";
    }
}
