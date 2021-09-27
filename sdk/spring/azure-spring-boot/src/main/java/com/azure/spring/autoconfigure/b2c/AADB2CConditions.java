// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * Conditions for activating AAD B2C beans.
 *
 * @deprecated All Azure AD B2C features supported by Spring security, please refer to https://github.com/zhichengliu12581/azure-spring-boot-samples/blob/add-samples-for-aad-b2c-with-only-spring-security/aad/aad-b2c-with-spring-security/README.adoc
 */
@Deprecated
public final class AADB2CConditions {

    /**
     * Web application or web resource server scenario condition.
     */
    static final class CommonCondition extends AnyNestedCondition {
        CommonCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        /**
         * Web application scenario condition.
         */
        @ConditionalOnWebApplication
        @ConditionalOnProperty(
            prefix = AADB2CProperties.PREFIX,
            value = {
                "client-id",
                "client-secret"
            }
        )
        static class WebAppMode {

        }

        /**
         * Web resource server scenario condition.
         */
        @ConditionalOnWebApplication
        @ConditionalOnProperty(prefix = AADB2CProperties.PREFIX, value = { "tenant-id" })
        static class WebApiMode {

        }
    }

    /**
     * OAuth2 client beans condition.
     */
    static final class ClientRegistrationCondition extends SpringBootCondition {

        @Override
        public ConditionOutcome getMatchOutcome(final ConditionContext context,
                                                final AnnotatedTypeMetadata metadata) {
            ConditionMessage.Builder message = ConditionMessage.forCondition(
                "AAD B2C OAuth 2.0 Clients Configured Condition");
            AADB2CProperties aadb2CProperties = getAADB2CProperties(context);
            if (aadb2CProperties == null) {
                return ConditionOutcome.noMatch(message.notAvailable("aad b2c properties"));
            }

            if (CollectionUtils.isEmpty(aadb2CProperties.getUserFlows())
                && CollectionUtils.isEmpty(aadb2CProperties.getAuthorizationClients())) {
                return ConditionOutcome.noMatch(message.didNotFind("registered clients")
                                                       .items("user-flows", "authorization-clients"));
            }

            StringBuilder details = new StringBuilder();
            if (!CollectionUtils.isEmpty(aadb2CProperties.getUserFlows())) {
                details.append(getConditionResult("user-flows", aadb2CProperties.getUserFlows()));
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
    static final class UserFlowCondition extends SpringBootCondition {

        @Override
        public ConditionOutcome getMatchOutcome(final ConditionContext context,
                                                final AnnotatedTypeMetadata metadata) {
            ConditionMessage.Builder message = ConditionMessage.forCondition(
                "AAD B2C User Flow Clients Configured Condition");
            AADB2CProperties aadb2CProperties = getAADB2CProperties(context);
            if (aadb2CProperties == null) {
                return ConditionOutcome.noMatch(message.notAvailable("aad b2c properties"));
            }

            if (CollectionUtils.isEmpty(aadb2CProperties.getUserFlows())) {
                return ConditionOutcome.noMatch(message.didNotFind("user flows").atAll());
            }

            return ConditionOutcome.match(message.foundExactly(
                getConditionResult("user-flows", aadb2CProperties.getUserFlows())));
        }
    }

    /**
     * Return the bound AADB2CProperties instance.
     * @param context Condition context
     * @return AADB2CProperties instance
     */
    private static AADB2CProperties getAADB2CProperties(ConditionContext context) {
        return Binder.get(context.getEnvironment())
                     .bind("azure.activedirectory.b2c", AADB2CProperties.class)
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
