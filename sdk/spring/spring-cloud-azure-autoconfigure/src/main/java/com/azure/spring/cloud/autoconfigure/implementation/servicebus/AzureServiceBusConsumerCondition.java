// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus;

import com.azure.spring.cloud.autoconfigure.implementation.servicebus.utils.AzureServiceBusPropertiesUtils;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

class AzureServiceBusConsumerCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {

        String entityType = AzureServiceBusPropertiesUtils.getServiceBusProperties(context, "consumer.entity-type", "entity-type");
        String consumerSubscriptionName = AzureServiceBusPropertiesUtils.getServiceBusProperties(context, "consumer.subscription-name");

        if ("queue".equalsIgnoreCase(entityType)) {
            return ConditionOutcome.match();
        }

        if ("topic".equalsIgnoreCase(entityType)) {
            if (consumerSubscriptionName != null) {
                return ConditionOutcome.match();
            } else {
                return ConditionOutcome.noMatch("spring.cloud.azure.servicebus.consumer.subscription-name is missing.");
            }
        }

        return ConditionOutcome.noMatch("Entity type should be queue/topic.");
    }
}
