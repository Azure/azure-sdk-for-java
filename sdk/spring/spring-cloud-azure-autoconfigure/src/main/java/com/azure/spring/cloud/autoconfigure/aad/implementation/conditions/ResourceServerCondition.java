// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.conditions;

import com.azure.spring.cloud.autoconfigure.aad.properties.AADApplicationType;

import java.util.function.Function;

import static com.azure.spring.cloud.autoconfigure.aad.properties.AADApplicationType.WEB_APPLICATION;

/**
 * Resource server or all in scenario condition.
 */
public final class ResourceServerCondition extends AbstractApplicationTypeCondition {

    @Override
    protected Function<AADApplicationType, Boolean> getNoMatchCondition() {
        return (applicationType) -> applicationType == null || applicationType == WEB_APPLICATION;
    }

    @Override
    protected String getConditionTitle() {
        return "AAD Resource Server Condition";
    }
}
