// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.spring.cloud.autoconfigure.jms.properties.AzureServiceBusJmsProperties;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.jupiter.api.Test;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.jms.connection.CachingConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceBusJmsAutoConfigurationTest extends AbstractServiceBusJmsAutoConfigurationTest {
    ServiceBusJmsAutoConfigurationTest() {
        this.contextRunner = super.contextRunner
            .withPropertyValues("spring.jms.servicebus.pricing-tier=premium")
            .withConfiguration(AutoConfigurations.of(ServiceBusJmsAutoConfiguration.class));
    }

    @Test
    void testAzureServiceBusAutoConfiguration() {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusJmsProperties.class);
                assertThat(context).hasBean("topicJmsListenerContainerFactory");
                assertThat(context).hasBean("jmsListenerContainerFactory");
            });
    }

    @Test
    void testWithoutServiceBusJmsNamespace() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(JmsConnectionFactory.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusJmsProperties.class));
    }

    @Test
    void testAzureServiceBusAutoConfigurationHasCachingConnectionFactoryBean() {
        this.contextRunner
            .withPropertyValues("spring.jms.servicebus.connection-string=" + CONNECTION_STRING)
            .run(context -> assertThat(context).hasSingleBean(CachingConnectionFactory.class));
    }

    @Test
    void testAzureServiceBusAutoConfigurationHasNativeConnectionFactoryBean() {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING,
                "spring.jms.cache.enabled=false"
            )
            .run(context -> assertThat(context).hasSingleBean(ServiceBusJmsConnectionFactory.class));
    }

    @Test
    void testAzureServiceBusAutoConfigurationHasJmsPoolConnectionFactoryBean() {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING,
                "spring.jms.servicebus.pool.enabled=true"
            )
            .run(context -> assertThat(context).hasSingleBean(JmsPoolConnectionFactory.class));
    }
}
