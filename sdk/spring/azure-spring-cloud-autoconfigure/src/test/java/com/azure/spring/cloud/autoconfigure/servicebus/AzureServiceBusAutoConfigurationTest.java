// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.context.AzureContextProperties;
import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.azure.spring.integration.servicebus.factory.ServiceBusConnectionStringProvider;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                assertThat(context.getBean(AzureServiceBusProperties.class).getTransportType()).isEqualTo(AmqpTransportType.AMQP);
            });
    }

    @Test
    public void testWithoutAzureResourceManagerProvided() {
        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(ServiceBusNamespaceManager.class));
    }

    @Test
    public void testWithoutServiceBusSDKInClasspath() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(ServiceBusReceivedMessage.class))
                          .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProperties.class));
    }

    @Test
    public void testAzureServiceBusPropertiesValidation() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(ServiceBusReceivedMessage.class))
                          .run(context -> assertThrows(NoSuchBeanDefinitionException.class,
                              () -> context.getBean(AzureServiceBusProperties.class)));
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
    public void testTransportTypeWithAmqpWebSockets() {
        this.contextRunner.withPropertyValues(SERVICE_BUS_PROPERTY_PREFIX + "transport-type=AMQP_WEB_SOCKETS")
                          .run(context -> {
                              assertThat(context.getBean(AzureServiceBusProperties.class).getTransportType()).isEqualTo(AmqpTransportType.AMQP_WEB_SOCKETS);
                          });
    }

    @Test
    public void testTransportTypeWithRetryOptions() {
        this.contextRunner.withPropertyValues(SERVICE_BUS_PROPERTY_PREFIX + "retry-options.maxRetries=5",
                                              SERVICE_BUS_PROPERTY_PREFIX + "retry-options.delay=100S",
                                              SERVICE_BUS_PROPERTY_PREFIX + "retry-options.maxDelay=200S",
                                              SERVICE_BUS_PROPERTY_PREFIX + "retry-options.tryTimeout=300S",
                                              SERVICE_BUS_PROPERTY_PREFIX + "retry-options.Mode=FIXED")
                          .run(context -> {
                              assertThat(context.getBean(AzureServiceBusProperties.class).getRetryOptions().getMaxRetries()).isEqualTo(5);
                              assertThat(context.getBean(AzureServiceBusProperties.class).getRetryOptions().getDelay().getSeconds()).isEqualTo(100L);
                              assertThat(context.getBean(AzureServiceBusProperties.class).getRetryOptions().getMaxDelay().getSeconds()).isEqualTo(200L);
                              assertThat(context.getBean(AzureServiceBusProperties.class).getRetryOptions().getTryTimeout().getSeconds()).isEqualTo(300L);
                              assertThat(context.getBean(AzureServiceBusProperties.class).getRetryOptions().getMode()).isEqualTo(AmqpRetryMode.FIXED);
                          });
    }

    @Test
    public void testWithAzureResourceManagerProvided() {
        this.contextRunner.withUserConfiguration(TestConfigWithAzureResourceManager.class)
                          .run(context -> assertThat(context).hasSingleBean(ServiceBusNamespaceManager.class));
    }

    @Configuration
    @Import(AzureServiceBusAutoConfiguration.class)
    @EnableConfigurationProperties(AzureContextProperties.class)
    public static class TestConfigWithAzureResourceManager {

        @Bean
        public AzureResourceManager azureResourceManager() {
            return mock(AzureResourceManager.class);
        }

    }


}
