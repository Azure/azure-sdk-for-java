// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.conditions;

import com.azure.spring.cloud.autoconfigure.aad.properties.AADApplicationType;
import org.springframework.context.annotation.Condition;

import static com.azure.spring.cloud.autoconfigure.aad.properties.AADApplicationType.WEB_APPLICATION;

/**
 * {@link Condition} that checks for resource server scenario.
 */
public final class ResourceServerCondition extends AbstractApplicationTypeCondition {

    @Override
    boolean isNonTargetApplicationType(AADApplicationType applicationType) {
        return applicationType == null || applicationType == WEB_APPLICATION;
    }

    @Override
    protected String getConditionTitle() {
        return "AAD Resource Server Condition";
    }
}
