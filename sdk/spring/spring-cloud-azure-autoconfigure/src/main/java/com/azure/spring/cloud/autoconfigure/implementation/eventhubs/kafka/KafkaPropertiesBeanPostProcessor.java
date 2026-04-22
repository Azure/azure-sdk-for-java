// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.eventhubs.kafka;

import com.azure.spring.cloud.core.implementation.connectionstring.EventHubsConnectionString;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ResolvableType;

import java.util.ArrayList;
import java.util.Collections;

import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.security.auth.SecurityProtocol.SASL_SSL;

/**
 * {@link BeanPostProcessor} for {@link KafkaProperties} to configure connection string credentials.
 */
class KafkaPropertiesBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPropertiesBeanPostProcessor.class);
    private static final String SASL_CONFIG_VALUE = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$ConnectionString\" password=\"%s\";%s";

    private ApplicationContext applicationContext;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof KafkaProperties) {
            //TODO(yiliu6): link to OAuth2 reference doc here
            LOGGER.warn("Autoconfiguration for Event Hubs for Kafka on connection string/Azure Resource Manager"
                + " has been deprecated, please migrate to AzureEventHubsKafkaOAuth2AutoConfiguration for OAuth2 authentication with Azure Identity credentials."
                + " To leverage the OAuth2 authentication, you can delete all your Event Hubs for Kafka credential configurations, and configure Kafka bootstrap servers"
                + " instead, which can be set as spring.kafka.boostrap-servers=EventHubsNamespacesFQDN:9093.");

            KafkaProperties kafkaProperties = (KafkaProperties) bean;
            ResolvableType provider = ResolvableType.forClassWithGenerics(ServiceConnectionStringProvider.class, AzureServiceType.EventHubs.class);
            ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.EventHubs>> beanProvider = applicationContext.getBeanProvider(provider);

            ServiceConnectionStringProvider<AzureServiceType.EventHubs> connectionStringProvider = beanProvider.getIfAvailable();
            if (connectionStringProvider == null) {
                LOGGER.debug("Cannot find a bean of type ServiceConnectionStringProvider<AzureServiceType.EventHubs>, "
                    + "Spring Cloud Azure will skip performing JAAS enhancements on the KafkaProperties bean.");
                return bean;
            }

            String connectionString = connectionStringProvider.getConnectionString();
            String bootstrapServer = new EventHubsConnectionString(connectionString).getFullyQualifiedNamespace() + ":9093";
            kafkaProperties.setBootstrapServers(new ArrayList<>(Collections.singletonList(bootstrapServer)));
            kafkaProperties.getProperties().put(SECURITY_PROTOCOL_CONFIG, SASL_SSL.name());
            kafkaProperties.getProperties().put(SASL_MECHANISM, "PLAIN");
            kafkaProperties.getProperties().put(SASL_JAAS_CONFIG, String.format(SASL_CONFIG_VALUE,
                connectionString, System.getProperty("line.separator")));
        }
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
