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
 *
 */
public class ServiceBusConditions {

    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnExpression(
        "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.consumer.connection-string:}') or "
            + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.consumer.namespace:}')"
    )
    public @interface ConditionalOnDedicatedServiceBusConsumer {
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
