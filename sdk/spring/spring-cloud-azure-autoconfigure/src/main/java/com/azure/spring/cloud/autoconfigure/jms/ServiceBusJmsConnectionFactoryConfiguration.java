// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.spring.cloud.autoconfigure.jms.properties.AzureServiceBusJmsProperties;
import org.apache.commons.pool2.PooledObject;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.JmsPoolConnectionFactoryFactory;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;

import javax.jms.ConnectionFactory;
import java.util.stream.Collectors;

/**
 * An auto-configuration for Service Bus JMS connection factory.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(ConnectionFactory.class)
public class ServiceBusJmsConnectionFactoryConfiguration {

    private static ServiceBusJmsConnectionFactory createJmsConnectionFactory(AzureServiceBusJmsProperties properties,
                                                                             ObjectProvider<ServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers) {
        return new ServiceBusJmsConnectionFactoryFactory(properties,
            factoryCustomizers.orderedStream().collect(Collectors.toList()))
            .createConnectionFactory(ServiceBusJmsConnectionFactory.class);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "spring.jms.servicebus.pool", name = "enabled", havingValue = "false",
        matchIfMissing = true)
    static class SimpleConnectionFactoryConfiguration {

        @Bean
        @ConditionalOnProperty(prefix = "spring.jms.cache", name = "enabled", havingValue = "false")
        ServiceBusJmsConnectionFactory jmsConnectionFactory(AzureServiceBusJmsProperties properties,
                                                            ObjectProvider<ServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers) {
            return createJmsConnectionFactory(properties, factoryCustomizers);
        }

        @Configuration(proxyBeanMethods = false)
        @ConditionalOnClass(CachingConnectionFactory.class)
        @ConditionalOnProperty(prefix = "spring.jms.cache", name = "enabled", havingValue = "true",
            matchIfMissing = true)
        static class CachingConnectionFactoryConfiguration {

            @Bean
            CachingConnectionFactory jmsConnectionFactory(JmsProperties jmsProperties,
                                                          AzureServiceBusJmsProperties properties,
                                                          ObjectProvider<ServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers) {
                ServiceBusJmsConnectionFactory factory = createJmsConnectionFactory(properties, factoryCustomizers);
                CachingConnectionFactory connectionFactory = new CachingConnectionFactory(factory);
                JmsProperties.Cache cacheProperties = jmsProperties.getCache();
                connectionFactory.setCacheConsumers(cacheProperties.isConsumers());
                connectionFactory.setCacheProducers(cacheProperties.isProducers());
                connectionFactory.setSessionCacheSize(cacheProperties.getSessionCacheSize());
                return connectionFactory;
            }
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({ JmsPoolConnectionFactory.class, PooledObject.class })
    static class PooledConnectionFactoryConfiguration {

        @Bean(destroyMethod = "stop")
        @ConditionalOnProperty(prefix = "spring.jms.servicebus.pool", name = "enabled", havingValue = "true")
        JmsPoolConnectionFactory jmsPoolConnectionFactory(AzureServiceBusJmsProperties properties,
                                                          ObjectProvider<ServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers) {
            ServiceBusJmsConnectionFactory factory = createJmsConnectionFactory(properties, factoryCustomizers);
            return new JmsPoolConnectionFactoryFactory(properties.getPool())
                .createPooledConnectionFactory(factory);
        }
    }
}
