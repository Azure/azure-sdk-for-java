// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.conditions;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadApplicationType;
import org.springframework.context.annotation.Condition;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadApplicationType.WEB_APPLICATION;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadApplicationType.WEB_APPLICATION_AND_RESOURCE_SERVER;

/**
 * {@link Condition} that checks for Web application scenario.
 */
public final class WebApplicationCondition extends AbstractApplicationTypeCondition {

    @Override
    boolean isTargetApplicationType(AadApplicationType applicationType) {
        return applicationType == WEB_APPLICATION || applicationType == WEB_APPLICATION_AND_RESOURCE_SERVER;
    }

    @Override
    protected String getConditionTitle() {
        return "AAD Web Application Condition";
    }
}
