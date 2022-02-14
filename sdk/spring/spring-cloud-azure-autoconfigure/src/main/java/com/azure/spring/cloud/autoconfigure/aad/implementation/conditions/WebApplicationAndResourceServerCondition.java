// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.conditions;

import com.azure.spring.cloud.autoconfigure.aad.properties.AADApplicationType;

import static com.azure.spring.cloud.autoconfigure.aad.properties.AADApplicationType.WEB_APPLICATION_AND_RESOURCE_SERVER;

/**
 * Web application and resource server scenario condition.
 */
public final class WebApplicationAndResourceServerCondition extends AbstractApplicationTypeCondition {

    @Override
    boolean isNonTargetApplicationType(AADApplicationType applicationType) {
        return applicationType != WEB_APPLICATION_AND_RESOURCE_SERVER;
    }

    @Override
    protected String getConditionTitle() {
        return "AAD Web Application and Resource Server Condition";
    }
}
