// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.binder.kafka.config.KafkaBinderConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Event Hubs Kafka with Spring Cloud Stream Binder.
 *
 * <p>This auto-configuration extends OAuth2 support to Spring Cloud Stream Kafka Binder, enabling passwordless
 * authentication for stream-based applications using Azure Event Hubs.</p>
 *
 * <h2>Purpose</h2>
 * <p>When Spring Cloud Stream Kafka Binder is detected on the classpath, this configuration ensures that
 * OAuth2 authentication is properly configured for all Kafka binder instances. It works by:</p>
 * <ul>
 *   <li>Detecting Spring Cloud Stream Kafka Binder on the classpath</li>
 *   <li>Registering {@link BindingServicePropertiesBeanPostProcessor} to process binder configurations</li>
 *   <li>Injecting OAuth2 configuration classes into the binder's application context</li>
 * </ul>
 *
 * <h2>Configuration Requirements</h2>
 * <p>This auto-configuration activates when:</p>
 * <ul>
 *   <li>{@code spring-cloud-stream-binder-kafka} is on the classpath</li>
 *   <li>{@code spring.cloud.azure.eventhubs.kafka.enabled} is true (default)</li>
 * </ul>
 *
 * <h2>Example Configuration</h2>
 * <pre>{@code
 * spring.kafka.bootstrap-servers=mynamespace.servicebus.windows.net:9093
 * spring.cloud.azure.credential.managed-identity-enabled=true
 * spring.cloud.stream.bindings.input.destination=my-event-hub
 * }</pre>
 *
 * @since 4.4.0
 * @see AzureEventHubsKafkaOAuth2AutoConfiguration
 * @see BindingServicePropertiesBeanPostProcessor
 * @see AzureKafkaSpringCloudStreamConfiguration
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(KafkaBinderConfiguration.class)
@ConditionalOnProperty(value = "spring.cloud.azure.eventhubs.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class AzureEventHubsKafkaBinderOAuth2AutoConfiguration {

    /**
     * Creates a BeanPostProcessor that configures OAuth2 authentication for Spring Cloud Stream Kafka binders.
     *
     * <p>This processor modifies {@link BindingServiceProperties} to inject OAuth2 configuration into
     * Kafka binder contexts, ensuring passwordless authentication works seamlessly with Spring Cloud Stream.</p>
     *
     * @return the BeanPostProcessor for binder configuration
     */
    @Bean
    static BeanPostProcessor bindingServicePropertiesBeanPostProcessor() {
        return new BindingServicePropertiesBeanPostProcessor();
    }
}
