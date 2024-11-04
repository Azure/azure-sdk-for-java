// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration.conditions;

import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration.properties.AadB2cProperties;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * Conditions for activating AAD B2C beans.
 */
public final class AadB2cConditions {

    private static final String KEY_OF_USER_FLOWS = "user-flows";
    /**
     * OAuth2 client beans condition.
     */
    public static final class ClientRegistrationCondition extends SpringBootCondition {

        @Override
        public ConditionOutcome getMatchOutcome(final ConditionContext context,
                                                final AnnotatedTypeMetadata metadata) {
            ConditionMessage.Builder message = ConditionMessage.forCondition(
                "AAD B2C OAuth 2.0 Clients Configured Condition");
            AadB2cProperties aadb2CProperties = getAadB2cProperties(context);
            if (aadb2CProperties == null) {
                return ConditionOutcome.noMatch(message.notAvailable("aad b2c properties"));
            }

            if (CollectionUtils.isEmpty(aadb2CProperties.getUserFlows())
                && CollectionUtils.isEmpty(aadb2CProperties.getAuthorizationClients())) {
                return ConditionOutcome.noMatch(message.didNotFind("registered clients")
                                                       .items(KEY_OF_USER_FLOWS, "authorization-clients"));
            }

            StringBuilder details = new StringBuilder();
            if (!CollectionUtils.isEmpty(aadb2CProperties.getUserFlows())) {
                details.append(getConditionResult(KEY_OF_USER_FLOWS, aadb2CProperties.getUserFlows()));
            }
            if (!CollectionUtils.isEmpty(aadb2CProperties.getAuthorizationClients())) {
                details.append(getConditionResult("authorization-clients",
                    aadb2CProperties.getAuthorizationClients()));
            }
            return ConditionOutcome.match(message.foundExactly(details.toString()));
        }
    }

    /**
     * AAD B2C OAuth2 security configuration condition.
     */
    public static final class UserFlowCondition extends SpringBootCondition {

        @Override
        public ConditionOutcome getMatchOutcome(final ConditionContext context,
                                                final AnnotatedTypeMetadata metadata) {
            ConditionMessage.Builder message = ConditionMessage.forCondition(
                "AAD B2C User Flow Clients Configured Condition");
            AadB2cProperties aadb2CProperties = getAadB2cProperties(context);
            if (aadb2CProperties == null) {
                return ConditionOutcome.noMatch(message.notAvailable("aad b2c properties"));
            }

            if (CollectionUtils.isEmpty(aadb2CProperties.getUserFlows())) {
                return ConditionOutcome.noMatch(message.didNotFind("user flows").atAll());
            }

            return ConditionOutcome.match(message.foundExactly(
                getConditionResult(KEY_OF_USER_FLOWS, aadb2CProperties.getUserFlows())));
        }
    }

    /**
     * Return the bound AADB2CProperties instance.
     * @param context Condition context
     * @return AADB2CProperties instance
     */
    private static AadB2cProperties getAadB2cProperties(ConditionContext context) {
        return Binder.get(context.getEnvironment())
                     .bind("spring.cloud.azure.active-directory.b2c", AadB2cProperties.class)
                     .orElse(null);
    }

    /**
     * Return combined name and the string of the keys of the map which concatenated with ','.
     * @param name name to concatenate
     * @param map Map to concatenate
     * @return the concatenated string.
     */
    private static String getConditionResult(String name, Map<String, ?> map) {
        return name + ": " + String.join(", ", map.keySet()) + " ";
    }
}
