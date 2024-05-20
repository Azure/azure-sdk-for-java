package com.azure.spring.cloud.autoconfigure.implementation.servicebus;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

class AzureServiceBusConsumerCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {

        Environment environment = context.getEnvironment();
        String entityType = environment.getProperty("spring.cloud.azure.servicebus.entity-type", "noType");
        String consumerEntityType = environment.getProperty("spring.cloud.azure.servicebus.consumer.entity-type","noType");
        String consumerSubscriptionName = environment.getProperty("spring.cloud.azure.servicebus.consumer.subscription-name", "noName");

        if ("queue".equalsIgnoreCase(entityType) || "queue".equalsIgnoreCase(consumerEntityType)) {
            return ConditionOutcome.match();
        }

        if ("topic".equalsIgnoreCase(entityType) || "topic".equalsIgnoreCase(consumerEntityType)) {
            if (!"noName".equals(consumerSubscriptionName)) {
                return ConditionOutcome.match();
            }
        }

        return ConditionOutcome.noMatch("Topic need to have subscription name set.");
    }
}
