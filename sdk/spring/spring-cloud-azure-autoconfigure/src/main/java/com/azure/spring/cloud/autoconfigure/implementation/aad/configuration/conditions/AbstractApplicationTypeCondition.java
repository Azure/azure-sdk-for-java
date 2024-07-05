// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.conditions;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadApplicationType;
import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Optional;

/**
 * Abstract class condition for each application type scenario.
 */
abstract class AbstractApplicationTypeCondition extends SpringBootCondition {

    /**
     * Check the applicationType satisfies the target application type.
     * @param applicationType the target application type.
     * @return true if the applicationType satisfies the target type condition.
     */
    abstract boolean isTargetApplicationType(AadApplicationType applicationType);

    private boolean isNotTargetApplicationType(AadApplicationType applicationType) {
        return !isTargetApplicationType(applicationType);
    }

    /**
     * Return the condition title name.
     * @return the condition title.
     */
    abstract String getConditionTitle();

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition(getConditionTitle());
        AadAuthenticationProperties properties =
            Binder.get(context.getEnvironment())
                  .bind("spring.cloud.azure.active-directory", AadAuthenticationProperties.class)
                  .orElse(null);
        if (properties == null) {
            return ConditionOutcome.noMatch(message.notAvailable("Azure AD authentication properties"));
        }

        // Bind properties will not execute AADAuthenticationProperties#afterPropertiesSet()
        AadApplicationType applicationType = Optional.ofNullable(properties.getApplicationType())
                                                     .orElseGet(AadApplicationType::inferApplicationTypeByDependencies);
        if (isNotTargetApplicationType(applicationType)) {
            return ConditionOutcome.noMatch(
                message.because("spring.cloud.azure.active-directory.application-type=" + applicationType));
        }
        return ConditionOutcome.match(
            message.foundExactly("spring.cloud.azure.active-directory.application-type=" + applicationType));
    }
}
