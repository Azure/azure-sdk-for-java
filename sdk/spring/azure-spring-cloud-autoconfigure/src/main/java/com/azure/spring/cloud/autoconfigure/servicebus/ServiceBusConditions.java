// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusConsumerClientConfiguration.CONSUMER_CLIENT_BUILDER_BEAN_NAME;

/**
 * Service Bus auto-configuration related conditions.
 */
public class ServiceBusConditions {

    /**
     * Condition indicates when service bus client should be auto-configured.
     */
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.connection-string:}') or "
                                 + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.namespace:}')")
    public @interface ConditionalOnServiceBusClient {
    }

    /**
     * Condition indicates when service bus consumer should be auto-configured.
     */
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnExpression(
        "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.consumer.queue-name:}') or "
            + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.consumer.topic-name:}')"
    )
    public @interface ConditionalOnServiceBusConsumer {
    }

    /**
     * Condition indicates when a service bus consumer client using dedicated connection should be auto-configured.
     */
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnExpression(
        "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.consumer.connection-string:}') or "
            + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.consumer.namespace:}')"
    )
    public @interface ConditionalOnDedicatedServiceBusConsumer {
    }

    /**
     * Condition indicates when service bus producer should be auto-configured.
     */
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.producer.queue-name:}') or "
                                 + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.producer.topic-name:}')")
    public @interface ConditionalOnServiceBusProducer {

    }

    /**
     * Condition indicates when a service bus producer client using dedicated connection should be auto-configured.
     */
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnExpression(
        "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.producer.connection-string:}') or "
            + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.producer.namespace:}')"
    )
    public @interface ConditionalOnDedicatedServiceBusProducer {
    }

    /**
     * Condition indicates when a service bus processor client using dedicated connection should be auto-configured.
     */
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnExpression(
        "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.processor.connection-string:}') or "
            + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.processor.producer.namespace:}')"
    )
    public @interface ConditionalOnDedicatedServiceBusProcessor {
    }

    /**
     * Condition indicates when service bus processor should be auto-configured.
     */
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnExpression(
        "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.processor.queue-name:}') or "
            + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.processor.topic-name:}')"
    )
    public @interface ConditionalOnServiceBusProcessor {
    }

    static class ConditionOnGlobalClientBuilderAndMissingReceiverClientBuilder extends AllNestedConditions {

        ConditionOnGlobalClientBuilderAndMissingReceiverClientBuilder() {

            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnMissingBean(name = CONSUMER_CLIENT_BUILDER_BEAN_NAME)
        static class OnMissingDedicatedClientBuilderForConsumer {
        }

        @ConditionalOnMissingBean(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class)
        static class OnMissingReceiverClientBuilder {
        }
    }

}
