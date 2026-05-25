// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureContextUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Set;

/**
 * Matches when no user-defined {@link EventHubClientBuilder} bean is present in the context.
 *
 * <p>Beans registered under the well-known auto-configuration bean names
 * ({@link AzureContextUtils#EVENT_HUB_CLIENT_BUILDER_BEAN_NAME},
 * {@link AzureContextUtils#EVENT_HUB_CONSUMER_CLIENT_BUILDER_BEAN_NAME},
 * {@link AzureContextUtils#EVENT_HUB_PRODUCER_CLIENT_BUILDER_BEAN_NAME}) are not considered
 * user-defined: the first is the root builder itself; the latter two are dedicated builders
 * created by the consumer/producer auto-configurations and must not suppress the root builder.
 */
class OnMissingUserDefinedEventHubClientBuilderCondition extends SpringBootCondition implements ConfigurationCondition {

    private static final Set<String> RESERVED_BUILDER_BEAN_NAMES = Set.of(
        AzureContextUtils.EVENT_HUB_CLIENT_BUILDER_BEAN_NAME,
        AzureContextUtils.EVENT_HUB_CONSUMER_CLIENT_BUILDER_BEAN_NAME,
        AzureContextUtils.EVENT_HUB_PRODUCER_CLIENT_BUILDER_BEAN_NAME
    );

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.REGISTER_BEAN;
    }

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
            context.getBeanFactory(), EventHubClientBuilder.class, true, false);
        for (String name : beanNames) {
            if (!RESERVED_BUILDER_BEAN_NAMES.contains(name)) {
                return ConditionOutcome.noMatch("found user-defined EventHubClientBuilder bean: " + name);
            }
        }
        return ConditionOutcome.match("no user-defined EventHubClientBuilder bean found");
    }
}
