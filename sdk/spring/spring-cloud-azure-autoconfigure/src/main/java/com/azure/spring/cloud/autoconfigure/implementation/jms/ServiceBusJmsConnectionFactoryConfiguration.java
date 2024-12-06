// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.azure.spring.cloud.autoconfigure.implementation.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.autoconfigure.jms.AzureServiceBusJmsConnectionFactoryCustomizer;
import jakarta.jms.ConnectionFactory;
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

import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(ConnectionFactory.class)
class ServiceBusJmsConnectionFactoryConfiguration  {

    private ServiceBusJmsConnectionFactory createJmsConnectionFactory(AzureServiceBusJmsProperties serviceBusJmsProperties,
                                                                             ObjectProvider<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers) {
        return new ServiceBusJmsConnectionFactoryFactory(serviceBusJmsProperties,
            factoryCustomizers.orderedStream().collect(Collectors.toList()))
            .createConnectionFactory(ServiceBusJmsConnectionFactory.class);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "spring.jms.servicebus.pool", name = "enabled", havingValue = "false",
        matchIfMissing = true)
    class SimpleConnectionFactoryConfiguration {


        @Bean
        @ConditionalOnProperty(prefix = "spring.jms.cache", name = "enabled", havingValue = "false")
        ServiceBusJmsConnectionFactory jmsConnectionFactory(AzureServiceBusJmsProperties properties,
                                                            ObjectProvider<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers) {
            return createJmsConnectionFactory(properties, factoryCustomizers);
        }

        @Configuration(proxyBeanMethods = false)
        @ConditionalOnClass(CachingConnectionFactory.class)
        @ConditionalOnProperty(prefix = "spring.jms.cache", name = "enabled", havingValue = "true",
            matchIfMissing = true)
        class CachingConnectionFactoryConfiguration {

            @Bean
            CachingConnectionFactory jmsConnectionFactory(JmsProperties jmsProperties,
                                                          AzureServiceBusJmsProperties properties,
                                                          ObjectProvider<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers) {
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
    class PooledConnectionFactoryConfiguration {

        @Bean(destroyMethod = "stop")
        @ConditionalOnProperty(prefix = "spring.jms.servicebus.pool", name = "enabled", havingValue = "true")
        JmsPoolConnectionFactory jmsPoolConnectionFactory(AzureServiceBusJmsProperties properties,
                                                          ObjectProvider<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers) {
            ServiceBusJmsConnectionFactory factory = createJmsConnectionFactory(properties, factoryCustomizers);
            return new JmsPoolConnectionFactoryFactory(properties.getPool())
                .createPooledConnectionFactory(factory);
        }
    }
}
