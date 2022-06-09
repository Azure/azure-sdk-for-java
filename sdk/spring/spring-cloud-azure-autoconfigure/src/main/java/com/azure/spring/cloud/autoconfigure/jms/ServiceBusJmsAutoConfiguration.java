// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnMissingProperty;
import com.azure.spring.cloud.autoconfigure.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureServiceBusResourceManagerAutoConfiguration;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import org.apache.qpid.jms.JmsConnectionExtensions;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.autoconfigure.jms.JndiConnectionFactoryAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier.AZURE_SPRING_SERVICE_BUS;

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
@EnableConfigurationProperties({ AzureServiceBusJmsProperties.class, JmsProperties.class })
@Import({ ServiceBusJmsConnectionFactoryConfiguration.class, ServiceBusJmsContainerConfiguration.class })
public class ServiceBusJmsAutoConfiguration {

    @Bean
    @ConditionalOnExpression("'premium'.equalsIgnoreCase('${spring.jms.servicebus.pricing-tier}')")
    ServiceBusJmsConnectionFactoryCustomizer amqpOpenPropertiesCustomizer() {
        return factory -> {
            final Map<String, Object> properties = new HashMap<>();
            properties.put("com.microsoft:is-client-provider", true);
            properties.put("user-agent", AZURE_SPRING_SERVICE_BUS);
            //set user agent
            factory.setExtension(JmsConnectionExtensions.AMQP_OPEN_PROPERTIES.toString(),
                (connection, uri) -> properties);
        };
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
}
