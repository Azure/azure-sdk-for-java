// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs.kafka;

import com.azure.spring.cloud.autoconfigure.eventhubs.AzureEventHubsAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureEventHubsResourceManagerAutoConfiguration;
import com.azure.spring.core.connectionstring.ConnectionStringProvider;
import com.azure.spring.core.connectionstring.implementation.EventHubsConnectionString;
import com.azure.spring.core.service.AzureServiceType;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.ArrayList;
import java.util.Collections;


/**
 * An auto-configuration for Azure Event Hubs Kafka, which provides {@link KafkaProperties}
 *
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(value = "spring.cloud.azure.eventhubs.kafka.enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter({ AzureEventHubsAutoConfiguration.class, AzureEventHubsResourceManagerAutoConfiguration.class })
public class AzureEventHubsKafkaAutoConfiguration {

    private static final String SASL_CONFIG_VALUE = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$ConnectionString\" password=\"%s\";%s";

    @Primary
    @Bean
    @ConditionalOnBean(value = AzureServiceType.EventHubs.class, parameterizedContainer = ConnectionStringProvider.class)
    public KafkaProperties azureKafkaProperties(
        ConnectionStringProvider<AzureServiceType.EventHubs> connectionStringProvider) {

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

