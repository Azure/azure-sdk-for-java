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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;


/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Event Hubs Kafka support.
 *
 * @since 4.0.0
 * @deprecated 4.3.0 in favor of {@link AzureEventHubsKafkaOAuth2AutoConfiguration}.
 */
@Deprecated
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(value = "spring.cloud.azure.eventhubs.kafka.enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter({ AzureEventHubsAutoConfiguration.class, AzureEventHubsResourceManagerAutoConfiguration.class })
public class AzureEventHubsKafkaAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureEventHubsKafkaAutoConfiguration.class);

    @Bean
    @ConditionalOnProperty("spring.cloud.azure.eventhubs.connection-string")
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

    @Bean
    @ConditionalOnBean(value = AzureServiceType.EventHubs.class, parameterizedContainer = ServiceConnectionStringProvider.class)
    static KafkaPropertiesBeanPostProcessor kafkaPropertiesBeanPostProcessor(
            ServiceConnectionStringProvider<AzureServiceType.EventHubs> connectionStringProvider) {
        return new KafkaPropertiesBeanPostProcessor(connectionStringProvider);
    }

}
