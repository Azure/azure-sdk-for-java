// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnMissingProperty;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureServiceBusResourceManagerAutoConfiguration;
import com.azure.spring.cloud.core.implementation.util.AzurePasswordlessPropertiesUtils;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.autoconfigure.jms.JndiConnectionFactoryAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Service Bus JMS support.
 *
 * @since 4.0.0
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(JmsAutoConfiguration.class)
@AutoConfigureAfter({
    JndiConnectionFactoryAutoConfiguration.class,
    AzureServiceBusResourceManagerAutoConfiguration.class })
@ConditionalOnProperty(value = "spring.jms.servicebus.enabled", matchIfMissing = true)
@ConditionalOnClass({ ConnectionFactory.class, JmsConnectionFactory.class, JmsTemplate.class })
@EnableConfigurationProperties({ JmsProperties.class })
@Import({ ServiceBusJmsConnectionFactoryConfiguration.class, ServiceBusJmsContainerConfiguration.class })
public class ServiceBusJmsAutoConfiguration {

    @Bean
    AzureServiceBusJmsProperties serviceBusJmsProperties(AzureGlobalProperties azureGlobalProperties) {
        AzureServiceBusJmsProperties properties = new AzureServiceBusJmsProperties();
        return mergeAzureProperties(azureGlobalProperties, properties);
    }

    /**
     * The BeanPostProcessor to instrument the {@link AzureServiceBusJmsProperties} bean with provided connection string
     * providers.
     * @param connectionStringProviders the connection string providers to provide the Service Bus connection string.
     * @return the bean post processor.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnMissingProperty(prefix = "spring.jms.servicebus", name = "connection-string")
    static AzureServiceBusJmsPropertiesBeanPostProcessor azureServiceBusJmsPropertiesBeanPostProcessor(
        ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.ServiceBus>> connectionStringProviders) {
        return new AzureServiceBusJmsPropertiesBeanPostProcessor(connectionStringProviders);
    }

    private AzureServiceBusJmsProperties mergeAzureProperties(AzureGlobalProperties azureGlobalProperties, AzureServiceBusJmsProperties azurePasswordlessProperties) {
        AzureServiceBusJmsProperties mergedProperties = new AzureServiceBusJmsProperties();
        AzurePasswordlessPropertiesUtils.mergeAzureCommonProperties(azureGlobalProperties, azurePasswordlessProperties, mergedProperties);
        return mergedProperties;
    }
}
