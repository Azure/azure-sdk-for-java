// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.eventhubs.kafka;

import com.azure.spring.cloud.autoconfigure.implementation.kafka.ConnectionStringAuthenticationConfigurer;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ResolvableType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import com.azure.spring.cloud.core.implementation.connectionstring.EventHubsConnectionString;

/**
 * {@link BeanPostProcessor} for {@link KafkaProperties} to configure connection string credentials.
 * 
 * @deprecated This class is deprecated in favor of OAuth2-based authentication.
 *             Use {@code AzureEventHubsKafkaOAuth2AutoConfiguration} instead.
 */
@Deprecated
class KafkaPropertiesBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPropertiesBeanPostProcessor.class);

    private ApplicationContext applicationContext;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof KafkaProperties kafkaProperties) {
            ResolvableType provider = ResolvableType.forClassWithGenerics(
                ServiceConnectionStringProvider.class, AzureServiceType.EventHubs.class);
            ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.EventHubs>> beanProvider = 
                applicationContext.getBeanProvider(provider);

            ServiceConnectionStringProvider<AzureServiceType.EventHubs> connectionStringProvider = 
                beanProvider.getIfAvailable();
            
            if (connectionStringProvider == null) {
                LOGGER.debug("Cannot find a bean of type ServiceConnectionStringProvider<AzureServiceType.EventHubs>, "
                    + "Spring Cloud Azure will skip performing connection string configuration on the KafkaProperties bean.");
                return bean;
            }

            // Set bootstrap servers from connection string
            String connectionString = connectionStringProvider.getConnectionString();
            String bootstrapServer = new EventHubsConnectionString(connectionString).getFullyQualifiedNamespace() + ":9093";
            kafkaProperties.setBootstrapServers(new ArrayList<>(Collections.singletonList(bootstrapServer)));

            // Use the ConnectionStringAuthenticationConfigurer to configure authentication
            ConnectionStringAuthenticationConfigurer configurer = 
                new ConnectionStringAuthenticationConfigurer(connectionStringProvider, LOGGER);
            
            Map<String, Object> mergedProperties = kafkaProperties.buildProducerProperties(null);
            if (configurer.canConfigure(mergedProperties)) {
                configurer.configure(mergedProperties, kafkaProperties.getProperties());
            }
        }
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
