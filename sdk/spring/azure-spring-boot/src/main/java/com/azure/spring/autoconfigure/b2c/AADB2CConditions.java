// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.CollectionUtils;

/**
 * Conditions for activating AAD B2C beans.
 */
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
        @ConditionalOnResource(resources = "classpath:aadb2c.enable.config")
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
        @ConditionalOnResource(resources = "classpath:aadb2c.enable.config")
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
            AADB2CProperties aadb2CProperties = Binder.get(context.getEnvironment())
                                                      .bind("azure.activedirectory.b2c", AADB2CProperties.class)
                                                      .orElseGet(AADB2CProperties::new);
            return new ConditionOutcome(!CollectionUtils.isEmpty(aadb2CProperties.getUserFlows())
                || !CollectionUtils.isEmpty(aadb2CProperties.getAuthorizationClients()),
                "Configure at least one attribute 'user-flow' or 'authorization-clients'.");
        }
    }

    /**
     * AAD B2C OAuth2 security configuration condition.
     */
    static final class UserFlowCondition extends SpringBootCondition {

        @Override
        public ConditionOutcome getMatchOutcome(final ConditionContext context,
                                                final AnnotatedTypeMetadata metadata) {
            AADB2CProperties aadb2CProperties = Binder.get(context.getEnvironment())
                                                      .bind("azure.activedirectory.b2c", AADB2CProperties.class)
                                                      .orElseGet(AADB2CProperties::new);
            return new ConditionOutcome(!CollectionUtils.isEmpty(aadb2CProperties.getUserFlows()),
                "Configure at least one attribute 'user-flow'.");
        }
    }
}
