// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.conditions;

import com.azure.spring.cloud.autoconfigure.aad.properties.AADApplicationType;

import java.util.function.Function;

import static com.azure.spring.cloud.autoconfigure.aad.properties.AADApplicationType.RESOURCE_SERVER_WITH_OBO;

/**
 * Resource server with OBO scenario condition.
 */
public final class ResourceServerWithOBOCondition extends AbstractApplicationTypeCondition {

    @Override
    protected Function<AADApplicationType, Boolean> getNoMatchCondition() {
        return (applicationType) -> applicationType != RESOURCE_SERVER_WITH_OBO;
    }

    @Override
    protected String getConditionTitle() {
        return "AAD Resource Server with OBO Condition";
    }
}
