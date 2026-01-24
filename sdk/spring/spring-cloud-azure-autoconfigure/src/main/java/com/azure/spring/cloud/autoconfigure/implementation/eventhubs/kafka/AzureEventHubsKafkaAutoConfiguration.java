// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs.kafka;

import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.AzureEventHubsAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureEventHubsKafkaOAuth2AutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.resourcemanager.AzureEventHubsResourceManagerAutoConfiguration;
import com.azure.spring.cloud.core.implementation.connectionstring.EventHubsConnectionString;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.provider.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;


/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Event Hubs Kafka support with connection string authentication.
 *
 * <p><strong>DEPRECATED:</strong> This auto-configuration is deprecated since version 4.3.0. Please migrate to
 * {@link AzureEventHubsKafkaOAuth2AutoConfiguration} which provides OAuth2-based authentication using Azure Identity.</p>
 *
 * <h2>Deprecation Notice</h2>
 * <p>This configuration uses connection string-based authentication (SASL_PLAIN) which is being phased out in favor
 * of more secure OAuth2 authentication. The OAuth2 approach provides:</p>
 * <ul>
 *   <li>Better security through Azure Active Directory integration</li>
 *   <li>Support for managed identities</li>
 *   <li>No need to store connection strings in configuration</li>
 *   <li>Automatic token rotation</li>
 * </ul>
 *
 * <h2>Migration Path</h2>
 * <p>To migrate from connection string to OAuth2:</p>
 * <pre>{@code
 * // Old configuration (deprecated)
 * spring.cloud.azure.eventhubs.connection-string=Endpoint=sb://...
 *
 * // New configuration (recommended)
 * spring.kafka.bootstrap-servers=mynamespace.servicebus.windows.net:9093
 * spring.cloud.azure.credential.managed-identity-enabled=true
 * }</pre>
 *
 * @since 4.0.0
 * @deprecated 4.3.0 in favor of {@link AzureEventHubsKafkaOAuth2AutoConfiguration}.
 * @see AzureEventHubsKafkaOAuth2AutoConfiguration
 */
@Deprecated
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(value = "spring.cloud.azure.eventhubs.kafka.enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter({ AzureEventHubsAutoConfiguration.class, AzureEventHubsResourceManagerAutoConfiguration.class })
public class AzureEventHubsKafkaAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureEventHubsKafkaAutoConfiguration.class);

    /**
     * Creates a connection string provider for Event Hubs Kafka when a connection string is configured.
     *
     * @param environment the Spring environment containing configuration properties
     * @return a connection string provider initialized with the Event Hubs connection string
     * @throws IllegalArgumentException if the connection string is invalid
     */
    @Bean
    @ConditionalOnExpression("'${spring.cloud.azure.eventhubs.connection-string:}' != ''")
    @ConditionalOnMissingBean(value = AzureServiceType.EventHubs.class, parameterizedContainer = ServiceConnectionStringProvider.class)
    StaticConnectionStringProvider<AzureServiceType.EventHubs> eventHubsKafkaConnectionString(Environment environment) {
        String connectionString = environment.getProperty("spring.cloud.azure.eventhubs.connection-string");

        try {
            new EventHubsConnectionString(connectionString);
        } catch (Exception e) {
            LOGGER.error("A valid Event Hubs connection string must be provided");
            throw e;
        }

        return new StaticConnectionStringProvider<>(AzureServiceType.EVENT_HUBS, connectionString);
    }

    /**
     * Creates a BeanPostProcessor that configures connection string-based authentication for KafkaProperties beans.
     *
     * @return the BeanPostProcessor for Kafka properties configuration
     */
    @Bean
    @ConditionalOnBean(value = AzureServiceType.EventHubs.class, parameterizedContainer = ServiceConnectionStringProvider.class)
    static KafkaPropertiesBeanPostProcessor kafkaPropertiesBeanPostProcessor() {
        return new KafkaPropertiesBeanPostProcessor();
    }
}
