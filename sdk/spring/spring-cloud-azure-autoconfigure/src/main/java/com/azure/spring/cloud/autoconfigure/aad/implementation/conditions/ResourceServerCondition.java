// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.conditions;

import com.azure.spring.cloud.autoconfigure.aad.properties.AADApplicationType;
import com.azure.spring.cloud.autoconfigure.aad.properties.AADAuthenticationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Optional;

import static com.azure.spring.cloud.autoconfigure.aad.properties.AADApplicationType.WEB_APPLICATION;

/**
 * Resource server or all in scenario condition.
 */
public final class ResourceServerCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition("AAD Resource Server Condition");
        AADAuthenticationProperties properties =
            Binder.get(context.getEnvironment())
                  .bind("spring.cloud.azure.active-directory", AADAuthenticationProperties.class)
                  .orElse(null);
        if (properties == null) {
            return ConditionOutcome.noMatch(message.notAvailable("aad authorization properties"));
        }

        // Bind properties will not execute AADAuthenticationProperties#afterPropertiesSet()
        AADApplicationType applicationType = Optional.ofNullable(properties.getApplicationType())
                                                     .orElseGet(AADApplicationType::inferApplicationTypeByDependencies);
        if (applicationType == null || applicationType == WEB_APPLICATION) {
            return ConditionOutcome.noMatch(
                message.because("spring.cloud.azure.active-directory.application-type=" + applicationType));
        }
        return ConditionOutcome.match(
            message.foundExactly("spring.cloud.azure.active-directory.application-type=" + applicationType));
    }
}
