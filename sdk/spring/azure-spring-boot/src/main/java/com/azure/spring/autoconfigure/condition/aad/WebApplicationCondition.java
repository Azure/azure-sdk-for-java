// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.condition.aad;

import com.azure.spring.aad.AADApplicationType;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

import java.util.Optional;

import static com.azure.spring.aad.AADApplicationType.RESOURCE_SERVER;
import static com.azure.spring.aad.AADApplicationType.RESOURCE_SERVER_WITH_OBO;

/**
 * Web application or all in scenario condition.
 */
public final class WebApplicationCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition("AAD Web Application Condition");
        AADAuthenticationProperties properties =
            Binder.get(context.getEnvironment())
                  .bind("azure.activedirectory", AADAuthenticationProperties.class)
                  .orElse(null);
        if (properties == null) {
            return ConditionOutcome.noMatch(message.notAvailable("aad authorization properties"));
        }

        if (!StringUtils.hasText(properties.getClientId())) {
            return ConditionOutcome.noMatch(message.didNotFind("client-id").atAll());
        }

        // Bind properties will not execute AADAuthenticationProperties#afterPropertiesSet()
        AADApplicationType applicationType = Optional.ofNullable(properties.getApplicationType())
                                                     .orElseGet(AADApplicationType::inferApplicationTypeByDependencies);
        if (applicationType == null
            || applicationType == RESOURCE_SERVER
            || applicationType == RESOURCE_SERVER_WITH_OBO) {
            return ConditionOutcome.noMatch(
                message.because("azure.activedirectory.application-type=" + applicationType));
        }
        return ConditionOutcome.match(
            message.foundExactly("azure.activedirectory.application-type=" + applicationType));
    }
}
