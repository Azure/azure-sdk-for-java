// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.CollectionUtils;

/**
 * Configuration condition for activating OAuth2 client related beans.
 */
public class ClientRegistrationCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext context,
                                            final AnnotatedTypeMetadata metadata) {
        AADB2CProperties aadb2CProperties = Binder.get(context.getEnvironment())
                                                  .bind("azure.activedirectory.b2c", AADB2CProperties.class)
                                                  .get();
        return new ConditionOutcome(!CollectionUtils.isEmpty(aadb2CProperties.getUserFlows())
            || !CollectionUtils.isEmpty(aadb2CProperties.getAuthorizationClients()),
            "Configure at least one attribute 'user-flow' or 'authorization-clients'.");
    }
}
