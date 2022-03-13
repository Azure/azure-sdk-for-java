// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.eventhubs.models.AuthorizationRule;
import com.azure.resourcemanager.eventhubs.models.EventHubAuthorizationKey;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceAuthorizationRule;
import com.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.cloud.context.core.impl.EventHubNamespaceManager;
import com.azure.spring.integration.eventhub.factory.EventHubConnectionStringProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;


/**
 * An auto-configuration for Event Hub, which provides {@link KafkaProperties}
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure.eventhub", value = "namespace")
@EnableConfigurationProperties(AzureEventHubProperties.class)
public class AzureEventHubKafkaAutoConfiguration {
    private static final String SECURITY_PROTOCOL = "security.protocol";
    private static final String SASL_SSL = "SASL_SSL";
    private static final String SASL_JAAS_CONFIG = "sasl.jaas.config";
    private static final String SASL_CONFIG_VALUE = "org.apache.kafka.common.security.plain.PlainLoginModule required"
        + " username=\"$ConnectionString\" " + "password=\"%s\";%n";
    private static final String SASL_MECHANISM = "sasl.mechanism";
    private static final String SASL_MECHANISM_PLAIN = "PLAIN";
    private static final int PORT = 9093;

    @SuppressWarnings("rawtypes")
    @Primary
    @Bean
    public KafkaProperties kafkaProperties(EventHubNamespaceManager eventHubNamespaceManager,
                                           AzureEventHubProperties eventHubProperties) {
        KafkaProperties kafkaProperties = new KafkaProperties();

        final EventHubNamespace namespace = eventHubNamespaceManager.getOrCreate(eventHubProperties.getNamespace());
        final String connectionString = toConnectionString(namespace);

        String endpoint = namespace.serviceBusEndpoint();
        String endpointHost = endpoint.substring("https://".length(), endpoint.lastIndexOf(':'));
        kafkaProperties.setBootstrapServers(Arrays.asList(endpointHost + ":" + PORT));
        kafkaProperties.getProperties().put(SECURITY_PROTOCOL, SASL_SSL);
        kafkaProperties.getProperties().put(SASL_MECHANISM, SASL_MECHANISM_PLAIN);
        kafkaProperties.getProperties().put(SASL_JAAS_CONFIG,
            String.format(SASL_CONFIG_VALUE, connectionString, System.getProperty("line.separator")));
        return kafkaProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(AzureResourceManager.class)
    public EventHubNamespaceManager eventHubNamespaceManager(AzureResourceManager azureResourceManager,
                                                             AzureProperties azureProperties) {
        return new EventHubNamespaceManager(azureResourceManager, azureProperties);
    }


    /**
     * The reason why not to use the {@link EventHubConnectionStringProvider} here is azure-spring-integration-eventhubs
     * is not included in the azure-spring-cloud-starter-eventhubs-kafka. Otherwise it will throw NoClassDefFoundError.
     *
     * @param eventHubNamespace the Event Hub namespace.
     * @return the connection string.
     */
    private static String toConnectionString(EventHubNamespace eventHubNamespace) {
        return eventHubNamespace.listAuthorizationRules()
                                .stream()
                                .findFirst()
                                .map(AuthorizationRule<EventHubNamespaceAuthorizationRule>::getKeys)
                                .map(EventHubAuthorizationKey::primaryConnectionString)
                                .orElseThrow(() -> new IllegalStateException(
                                    String.format("Failed to fetch connection string of namespace '%s'",
                                        eventHubNamespace.name()), null));
    }

}
