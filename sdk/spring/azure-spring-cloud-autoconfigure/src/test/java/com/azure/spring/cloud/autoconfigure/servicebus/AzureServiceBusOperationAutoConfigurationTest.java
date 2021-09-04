// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.spring.cloud.autoconfigure.commonconfig.TestConfigWithAzureResourceManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.azure.spring.core.converter.AzureAmqpRetryOptionsConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AzureServiceBusOperationAutoConfigurationTest {

    private static final AzureAmqpRetryOptionsConverter RETRY_OPTIONS_CONVERTER = new AzureAmqpRetryOptionsConverter();

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusOperationAutoConfiguration.class));

    @Test
    public void testAzureServiceBusDefault() {
        this.contextRunner.run(context -> assertThat(context).hasSingleBean(AzureServiceBusProperties.class));
    }

    @Test
    public void testAzureServiceBusDisabled() {
        this.contextRunner.withPropertyValues(AzureServiceBusProperties.PREFIX + ".enabled=false")
                          .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProperties.class));
    }

    @Test
    public void testAzureServiceBusPropertiesConfigured() {
        this.contextRunner
            .withPropertyValues(
                AzureServiceBusProperties.PREFIX + ".namespace=ns1",
                AzureServiceBusProperties.PREFIX + ".connection-string=str1")
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
        this.contextRunner.withPropertyValues(AzureServiceBusProperties.PREFIX + ".connection-string=str1")
                          .run(context -> {
                              assertThat(context.getBean(ServiceBusConnectionStringProvider.class).getConnectionString()).isEqualTo("str1");
                              assertThat(context).doesNotHaveBean(ServiceBusNamespaceManager.class);
                          });
    }

    @Test
    public void testTransportTypeWithAmqpWebSockets() {
        this.contextRunner.withPropertyValues(AzureServiceBusProperties.PREFIX + ".client.transport-type=AmqpWebSockets")
                          .run(context -> assertThat(context.getBean(AzureServiceBusProperties.class)
                                                            .getClient()
                                                            .getTransportType())
                              .isEqualTo(AmqpTransportType.AMQP_WEB_SOCKETS));
    }

    @Test
    public void testRetryOptions() {
        this.contextRunner.withPropertyValues(AzureServiceBusProperties.PREFIX + ".retry.max-attempts=5",
                                              AzureServiceBusProperties.PREFIX + ".retry.timeout=3000",
                                              AzureServiceBusProperties.PREFIX + ".retry.backoff.delay=1000",
                                              AzureServiceBusProperties.PREFIX + ".retry.backoff.maxDelay=2000")
                          .run(context -> {
                              final AzureServiceBusProperties properties = context.getBean(
                                  AzureServiceBusProperties.class);
                              final AmqpRetryOptions retryOptions = RETRY_OPTIONS_CONVERTER.convert(properties.getRetry());

                              assertThat(retryOptions.getMaxRetries()).isEqualTo(5);
                              assertThat(retryOptions.getTryTimeout().getSeconds()).isEqualTo(3L);
                              assertThat(retryOptions.getDelay().getSeconds()).isEqualTo(1);
                              assertThat(retryOptions.getMaxDelay().getSeconds()).isEqualTo(2);
                              assertThat(retryOptions.getMode()).isEqualTo(AmqpRetryMode.FIXED);
                          });
    }

    @Test
    public void testWithAzureResourceManagerProvided() {
        this.contextRunner.withUserConfiguration(TestConfigWithAzureResourceManager.class)
                          .run(context -> assertThat(context).hasSingleBean(ServiceBusNamespaceManager.class));
    }


}
