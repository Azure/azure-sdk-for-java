// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.azure.spring.integration.servicebus.factory.ServiceBusConnectionStringProvider;
import com.microsoft.azure.servicebus.IMessage;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AzureServiceBusAutoConfigurationTest {

    private static final String SERVICE_BUS_PROPERTY_PREFIX = "spring.cloud.azure.servicebus.";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusAutoConfiguration.class));

    @Test
    public void testAzureServiceBusDefault() {
        this.contextRunner.run(context -> assertThat(context).hasSingleBean(AzureServiceBusProperties.class));
    }

    @Test
    public void testAzureServiceBusDisabled() {
        this.contextRunner.withPropertyValues(SERVICE_BUS_PROPERTY_PREFIX + "enabled=false")
                          .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProperties.class));
    }

    @Test
    public void testAzureServiceBusPropertiesConfigured() {
        this.contextRunner
            .withPropertyValues(
                SERVICE_BUS_PROPERTY_PREFIX + "namespace=ns1",
                SERVICE_BUS_PROPERTY_PREFIX + "connection-string=str1")
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProperties.class);
                assertThat(context.getBean(AzureServiceBusProperties.class).getNamespace()).isEqualTo("ns1");
                assertThat(context.getBean(AzureServiceBusProperties.class).getConnectionString()).isEqualTo("str1");
            });
    }

    @Test
    public void testWithoutAzureResourceManagerProvided() {
        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(ServiceBusNamespaceManager.class));
    }

    @Test
    public void testWithoutServiceBusSDKInClasspath() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(IMessage.class))
                          .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProperties.class));
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void testAzureServiceBusPropertiesValidation() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(IMessage.class))
                          .run(context -> context.getBean(AzureServiceBusProperties.class));
    }

    @Test
    public void testConnectionStringProviderNull() {
        // Spring will use NullBean for bean of value null
        this.contextRunner.run(context -> assertThat(context.getBean("serviceBusConnectionStringProvider")
                                                            .equals(null)));
    }

    @Test
    public void testConnectionStringProvided() {
        this.contextRunner.withPropertyValues(SERVICE_BUS_PROPERTY_PREFIX + "connection-string=str1")
                          .run(context -> {
                              assertThat(context.getBean(ServiceBusConnectionStringProvider.class).getConnectionString()).isEqualTo("str1");
                              assertThat(context).doesNotHaveBean(ServiceBusNamespaceManager.class);
                          });
    }

    @Test
    public void testWithAzureResourceManagerProvided() {
        this.contextRunner.withUserConfiguration(TestConfigWithAzureResourceManager.class)
                          .run(context -> assertThat(context).hasSingleBean(ServiceBusNamespaceManager.class));
    }

    @Configuration
    @Import(AzureServiceBusAutoConfiguration.class)
    @EnableConfigurationProperties(AzureProperties.class)
    public static class TestConfigWithAzureResourceManager {

        @Bean
        public AzureResourceManager azureResourceManager() {
            return mock(AzureResourceManager.class);
        }

    }


}
