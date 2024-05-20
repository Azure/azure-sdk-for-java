// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

class AzureServiceBusProcessorCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {

        Environment environment = context.getEnvironment();
        String entityType = environment.getProperty("spring.cloud.azure.servicebus.entity-type", "noType");
        String processorEntityType = environment.getProperty("spring.cloud.azure.servicebus.processor.entity-type","noType");
        String processorSubscriptionName = environment.getProperty("spring.cloud.azure.servicebus.processor.subscription-name", "noName");

        if ("queue".equalsIgnoreCase(entityType) || "queue".equalsIgnoreCase(processorEntityType)) {
            return ConditionOutcome.match();
        }

        if ("topic".equalsIgnoreCase(entityType) || "topic".equalsIgnoreCase(processorEntityType)) {
            if (!"noName".equals(processorSubscriptionName)) {
                return ConditionOutcome.match();
            }
        }

        return ConditionOutcome.noMatch("Topic need to have subscription name set.");
    }
}
