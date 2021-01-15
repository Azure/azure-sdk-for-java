// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusTopicManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusTopicSubscriptionManager;
import com.azure.spring.integration.servicebus.factory.ServiceBusConnectionStringProvider;
import com.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import com.microsoft.azure.servicebus.TopicClient;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AzureServiceBusTopicAutoConfigurationTest {

    private static final String SERVICE_BUS_PROPERTY_PREFIX = "spring.cloud.azure.servicebus.";
    private static final String AZURE_PROPERTY_PREFIX = "spring.cloud.azure.";

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusTopicAutoConfiguration.class));

    @Test
    public void testAzureServiceBusTopicDisabled() {
        this.contextRunner.withPropertyValues(SERVICE_BUS_PROPERTY_PREFIX + "enabled=false")
                          .run(context -> assertThat(context).doesNotHaveBean(ServiceBusTopicOperation.class));
    }

    @Test
    public void testWithoutAzureServiceBusTopicClient() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(TopicClient.class))
                          .run(context -> assertThat(context).doesNotHaveBean(ServiceBusTopicOperation.class));
    }

    @Test
    public void testWithoutServiceBusNamespaceManager() {
        this.contextRunner.withUserConfiguration(TestConfigWithConnectionStringProvider.class)
                          .run(c -> assertThat(c).doesNotHaveBean(ServiceBusTopicManager.class)
                                                 .doesNotHaveBean(ServiceBusTopicSubscriptionManager.class));
    }

    @Test
    public void testWithServiceBusNamespaceManager() {
        this.contextRunner.withUserConfiguration(TestConfigWithConnectionStringProvider.class,
            TestConfigWithServiceBusNamespaceManager.class)
                          .run(context -> assertThat(context).hasSingleBean(ServiceBusTopicManager.class)
                                                             .hasSingleBean(ServiceBusTopicSubscriptionManager.class));
    }

    @Test
    public void testTopicClientFactoryCreated() {
        this.contextRunner.withUserConfiguration(TestConfigWithConnectionStringProvider.class, TestConfigWithServiceBusNamespaceManager.class)
                          .run(context -> assertThat(context).hasSingleBean(ServiceBusTopicClientFactory.class)
                                                             .hasSingleBean(ServiceBusTopicOperation.class));
    }

    @Test
    public void testConnectionStringProvided() {
        this.contextRunner.withPropertyValues(SERVICE_BUS_PROPERTY_PREFIX + "connection-string=str1")
                          .withUserConfiguration(AzureServiceBusAutoConfiguration.class)
                          .run(context -> {
                              assertThat(context.getBean(ServiceBusConnectionStringProvider.class).getConnectionString()).isEqualTo("str1");
                              assertThat(context).doesNotHaveBean(ServiceBusNamespaceManager.class);
                              assertThat(context).doesNotHaveBean(ServiceBusTopicManager.class);
                              assertThat(context).doesNotHaveBean(ServiceBusTopicSubscriptionManager.class);
                              assertThat(context).hasSingleBean(ServiceBusTopicClientFactory.class);
                              assertThat(context).hasSingleBean(ServiceBusTopicOperation.class);
                          });
    }

    @Test
    public void testResourceManagerProvided() {
        this.contextRunner.withUserConfiguration(TestConfigWithAzureResourceManager.class, TestConfigWithConnectionStringProvider.class, AzureServiceBusAutoConfiguration.class)
                          .withPropertyValues(
                              AZURE_PROPERTY_PREFIX + "resource-group=rg1",
                              SERVICE_BUS_PROPERTY_PREFIX + "namespace=ns1"
                          )
                          .run(context -> {
                              assertThat(context).hasSingleBean(ServiceBusTopicClientFactory.class);
                              assertThat(context).hasSingleBean(ServiceBusTopicOperation.class);
                              assertThat(context).hasSingleBean(ServiceBusNamespaceManager.class);
                              assertThat(context).hasSingleBean(ServiceBusTopicManager.class);
                              assertThat(context).hasSingleBean(ServiceBusTopicSubscriptionManager.class);
                          });
    }

    @Configuration
    @EnableConfigurationProperties(AzureProperties.class)
    public static class TestConfigWithServiceBusNamespaceManager {

        @Bean
        public ServiceBusNamespaceManager servicebusNamespaceManager() {
            return mock(ServiceBusNamespaceManager.class);
        }

    }

    @Configuration
    @EnableConfigurationProperties(AzureServiceBusProperties.class)
    public static class TestConfigWithConnectionStringProvider {

        @Bean
        public ServiceBusConnectionStringProvider serviceBusConnectionStringProvider() {
            return new ServiceBusConnectionStringProvider("fake");
        }

    }

    @Configuration
    @EnableConfigurationProperties(AzureProperties.class)
    public static class TestConfigWithAzureResourceManager {

        @Bean
        public AzureResourceManager azureResourceManager() {
            return mock(AzureResourceManager.class);
        }


    }
}
