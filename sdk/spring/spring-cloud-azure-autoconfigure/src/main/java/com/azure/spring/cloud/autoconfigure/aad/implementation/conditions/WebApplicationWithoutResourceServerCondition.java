// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.conditions;

import com.azure.spring.cloud.autoconfigure.aad.properties.AADApplicationType;

import static com.azure.spring.cloud.autoconfigure.aad.properties.AADApplicationType.WEB_APPLICATION;

/**
 * Web application without Resource Server scenario condition.
 */
public final class WebApplicationWithoutResourceServerCondition extends AbstractApplicationTypeCondition {

    @Override
    boolean isNonTargetApplicationType(AADApplicationType applicationType) {
        return applicationType != WEB_APPLICATION;
    }

    @Override
    protected String getConditionTitle() {
        return "AAD Web application Condition";
    }
}
