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

import static com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadApplicationType.RESOURCE_SERVER;

/**
 * Web application, web resource server or all in scenario condition.
 */
public final class ClientRegistrationCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition("AAD Application Client Condition");
        AadAuthenticationProperties properties =
            Binder.get(context.getEnvironment())
                  .bind("spring.cloud.azure.active-directory", AadAuthenticationProperties.class)
                  .orElse(null);
        if (properties == null) {
            return ConditionOutcome.noMatch(
                message.notAvailable("AAD authorization properties(spring.cloud.azure.active-directory" + ".xxx)"));
        }

        // Bind properties will not execute AADAuthenticationProperties#afterPropertiesSet()
        AadApplicationType applicationType = Optional.ofNullable(properties.getApplicationType())
                                                     .orElseGet(AadApplicationType::inferApplicationTypeByDependencies);
        if (applicationType == null || applicationType == RESOURCE_SERVER) {
            return ConditionOutcome.noMatch(
                message.because("Resource server does not need client registration."));
        }
        return ConditionOutcome.match(
            message.foundExactly("spring.cloud.azure.active-directory.application-type=" + applicationType));
    }
}
