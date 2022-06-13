// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs.kafka;

import com.azure.spring.cloud.autoconfigure.eventhubs.AzureEventHubsAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureEventHubsResourceManagerAutoConfiguration;
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
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.ArrayList;
import java.util.Collections;


/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Event Hubs Kafka support.
 *
 * @since 4.0.0
 * @deprecated 4.3.0
 */
@Deprecated
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(value = "spring.cloud.azure.eventhubs.kafka.enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter({ AzureEventHubsAutoConfiguration.class, AzureEventHubsResourceManagerAutoConfiguration.class })
public class AzureEventHubsKafkaAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureEventHubsKafkaAutoConfiguration.class);
    private static final String SASL_CONFIG_VALUE = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$ConnectionString\" password=\"%s\";%s";

    /**
     * The static connection string provider to provide the connection string for an Event Hubs instance.
     * @param environment the Spring environment.
     * @return the connection string provider.
     */
    @Bean
    @ConditionalOnProperty("spring.cloud.azure.eventhubs.connection-string")
    @ConditionalOnMissingBean(value = AzureServiceType.EventHubs.class, parameterizedContainer = ServiceConnectionStringProvider.class)
    public StaticConnectionStringProvider<AzureServiceType.EventHubs> eventHubsKafkaConnectionString(Environment environment) {
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
     * The Azure {@link KafkaProperties} instance will be created if an Azure Event Hubs connection string is provided
     * and the Kafka dependency is detected from the classpath.
     * @param connectionStringProvider the Azure Event Hubs connection string provider.
     * @return the {@link KafkaProperties} with an Azure Event Hubs connection information.
     */
    @Primary
    @Bean
    @ConditionalOnBean(value = AzureServiceType.EventHubs.class, parameterizedContainer = ServiceConnectionStringProvider.class)
    public KafkaProperties azureKafkaProperties(
            ServiceConnectionStringProvider<AzureServiceType.EventHubs> connectionStringProvider) {

        LOGGER.warn("Autoconfiguration for Event Hubs for Kafka on connection string/Azure Resource Manager"
            + " has been deprecated, please use OAuth2 instead.");

        KafkaProperties kafkaProperties = new KafkaProperties();

        String connectionString = connectionStringProvider.getConnectionString();

        String bootstrapServer = new EventHubsConnectionString(connectionString).getFullyQualifiedNamespace() + ":9093";
        kafkaProperties.setBootstrapServers(new ArrayList<>(Collections.singletonList(bootstrapServer)));
        kafkaProperties.getProperties().put("security.protocol", "SASL_SSL");
        kafkaProperties.getProperties().put("sasl.mechanism", "PLAIN");
        kafkaProperties.getProperties().put("sasl.jaas.config", String.format(SASL_CONFIG_VALUE,
                connectionString, System.getProperty("line.separator")));
        return kafkaProperties;
    }

}
