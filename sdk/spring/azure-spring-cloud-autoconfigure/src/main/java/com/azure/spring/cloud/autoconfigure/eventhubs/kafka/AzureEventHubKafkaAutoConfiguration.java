// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs.kafka;

import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubProperties;
import com.azure.spring.core.ConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;


/**
 * An auto-configuration for Event Hub, which provides {@link KafkaProperties}
 *
 * @author Warren Zhu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(KafkaTemplate.class)
//@EnableConfigurationProperties(AzureEventHubProperties.class)
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
    // TODO (xiada): refactor this logic
    public KafkaProperties kafkaProperties(
        AzureEventHubProperties eventHubProperties,
        ObjectProvider<ConnectionStringProvider<AzureServiceType.EventHub>> connectionStringProviders) {
        KafkaProperties kafkaProperties = new KafkaProperties();
/*
        String endpoint = namespace.serviceBusEndpoint();
        String endpointHost = endpoint.substring("https://".length(), endpoint.lastIndexOf(':'));
        kafkaProperties.setBootstrapServers(Arrays.asList(endpointHost + ":" + PORT));
        kafkaProperties.getProperties().put(SECURITY_PROTOCOL, SASL_SSL);
        kafkaProperties.getProperties().put(SASL_MECHANISM, SASL_MECHANISM_PLAIN);
        kafkaProperties.getProperties().put(SASL_JAAS_CONFIG,
            String.format(SASL_CONFIG_VALUE, connectionStringProviders.getIfAvailable().getConnectionString(), System.getProperty("line.separator")));*/
        return kafkaProperties;
    }


}
