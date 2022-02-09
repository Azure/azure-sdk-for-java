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
import java.util.function.Function;

/**
 * Abstract class condition for each application type scenario.
 */
public abstract class AbstractApplicationTypeCondition extends SpringBootCondition {

    /**
     * Return function that application type does not match the condition.
     * @return the no match condition function.
     */
    protected abstract Function<AADApplicationType, Boolean> getNoMatchCondition();

    /**
     * Return the condition title name.
     * @return the condition title.
     */
    protected abstract String getConditionTitle();

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return getApplicationTypeConditionOutcome(context);
    }

    /**
     * Determine the outcome based on application type properties.
     * @param context the condition context
     * @return the condition outcome
     */
    private ConditionOutcome getApplicationTypeConditionOutcome(ConditionContext context) {
        ConditionMessage.Builder message = ConditionMessage.forCondition(getConditionTitle());
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
        if (getNoMatchCondition().apply(applicationType)) {
            return ConditionOutcome.noMatch(
                message.because("spring.cloud.azure.active-directory.application-type=" + applicationType));
        }
        return ConditionOutcome.match(
            message.foundExactly("spring.cloud.azure.active-directory.application-type=" + applicationType));
    }
}
