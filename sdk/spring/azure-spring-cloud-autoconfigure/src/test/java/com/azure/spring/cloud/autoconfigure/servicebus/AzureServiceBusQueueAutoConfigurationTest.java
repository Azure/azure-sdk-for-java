// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusQueueManager;
import com.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import com.microsoft.azure.servicebus.QueueClient;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AzureServiceBusQueueAutoConfigurationTest {

    private static final String SERVICE_BUS_PROPERTY_PREFIX = "spring.cloud.azure.servicebus.";

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusQueueAutoConfiguration.class));

    @Test
    public void testAzureServiceBusDisabled() {
        this.contextRunner.withPropertyValues(SERVICE_BUS_PROPERTY_PREFIX + "enabled=false")
                          .run(context -> assertThat(context).doesNotHaveBean(ServiceBusQueueOperation.class));
    }

    @Test
    public void testWithoutAzureServiceBusQueueClient() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(QueueClient.class))
                          .run(context -> assertThat(context).doesNotHaveBean(ServiceBusQueueOperation.class));
    }

    @Test
    public void testWithoutServiceBusNamespaceManager() {
        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(ServiceBusQueueManager.class));
    }

    @Test
    public void testWithServiceBusNamespaceManager() {
        this.contextRunner.withUserConfiguration(TestConfigWithServiceBusNamespaceManager.class)
                          .run(context -> assertThat(context).hasSingleBean(ServiceBusQueueManager.class));
    }

    @Test
    public void testQueueClientFactoryCreated() {
        this.contextRunner.withUserConfiguration(AzureServiceBusAutoConfiguration.class,
            TestConfigWithServiceBusNamespaceManager.class)
                          .withPropertyValues(SERVICE_BUS_PROPERTY_PREFIX + "connection-string=str1")
                          .run(context -> assertThat(context).hasSingleBean(ServiceBusQueueClientFactory.class)
                                                             .hasSingleBean(ServiceBusQueueOperation.class));
    }

    @Test
    public void testQueueClientFactoryNotCreated() {
        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(ServiceBusQueueClientFactory.class)
                                                             .doesNotHaveBean(ServiceBusQueueOperation.class));
    }

    @Configuration
    @EnableConfigurationProperties(AzureProperties.class)
    public static class TestConfigWithServiceBusNamespaceManager {

        @Bean
        public ServiceBusNamespaceManager servicebusNamespaceManager() {
            return mock(ServiceBusNamespaceManager.class);
        }

    }


}
