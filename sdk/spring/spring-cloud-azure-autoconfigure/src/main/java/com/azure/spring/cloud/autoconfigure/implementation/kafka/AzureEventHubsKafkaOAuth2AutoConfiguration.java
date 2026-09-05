// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import static com.azure.spring.cloud.autoconfigure.implementation.context.AzureContextUtils.PASSWORDLESS_KAFKA_PROPERTIES_BEAN_POST_PROCESSOR_BEAN_NAME;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Event Hubs Kafka support with OAuth2 authentication.
 *
 * <p>This auto-configuration provides Azure Identity-based OAuth2 (OAUTHBEARER) authentication for Azure Event Hubs
 * for Kafka. It automatically configures Kafka properties to use Azure Active Directory credentials instead of
 * connection strings.</p>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Automatic OAuth2 configuration for Kafka clients</li>
 *   <li>Support for all Azure Identity credential types (Managed Identity, Service Principal, etc.)</li>
 *   <li>Works with Spring Boot's standard Kafka configuration</li>
 *   <li>No need to manually configure SASL/OAUTHBEARER settings</li>
 * </ul>
 *
 * <h2>Configuration Requirements</h2>
 * <p>To use this auto-configuration, ensure:</p>
 * <ul>
 *   <li>Kafka client libraries are on the classpath</li>
 *   <li>Bootstrap servers point to Event Hubs namespace (*.servicebus.windows.net:9093)</li>
 *   <li>Azure Identity credentials are properly configured</li>
 * </ul>
 *
 * <h2>Example Configuration</h2>
 * <pre>{@code
 * spring.kafka.bootstrap-servers=mynamespace.servicebus.windows.net:9093
 * spring.cloud.azure.credential.managed-identity-enabled=true
 * }</pre>
 *
 * @since 4.3.0
 * @see KafkaPropertiesBeanPostProcessor
 * @see OAuth2AuthenticationConfigurer
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(value = "spring.cloud.azure.eventhubs.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class AzureEventHubsKafkaOAuth2AutoConfiguration {

    /**
     * Creates a BeanPostProcessor that configures OAuth2 authentication for KafkaProperties beans.
     *
     * @return the BeanPostProcessor for Kafka properties configuration
     */
    @Bean(PASSWORDLESS_KAFKA_PROPERTIES_BEAN_POST_PROCESSOR_BEAN_NAME)
    static BeanPostProcessor kafkaPropertiesBeanPostProcessor() {
        return new KafkaPropertiesBeanPostProcessor();
    }
}
