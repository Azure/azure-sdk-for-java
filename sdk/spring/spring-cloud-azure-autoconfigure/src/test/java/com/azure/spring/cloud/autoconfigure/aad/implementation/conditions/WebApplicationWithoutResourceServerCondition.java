// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.conditions;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadApplicationType;
import org.springframework.context.annotation.Condition;

import static com.azure.spring.cloud.autoconfigure.aad.properties.AadApplicationType.WEB_APPLICATION;

/**
 * {@link Condition} that checks for Web application without Resource Server scenario.
 */
public final class WebApplicationWithoutResourceServerCondition extends AbstractApplicationTypeCondition {

    @Override
    boolean isTargetApplicationType(AadApplicationType applicationType) {
        return applicationType == WEB_APPLICATION;
    }

    @Override
    protected String getConditionTitle() {
        return "AAD Web application Condition";
    }
}
