// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.cloud.autoconfigure.commonconfig.TestConfigWithAzureResourceManager;
import com.azure.spring.cloud.autoconfigure.context.AzureContextProperties;
import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusTopicManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusTopicSubscriptionManager;
import com.azure.spring.core.converter.AzureAmqpRetryOptionsConverter;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import com.azure.spring.integration.servicebus.topic.ServiceBusTopicTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusQueueOperationAutoConfigurationTest.NAMESPACE_CONNECTION_STRING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

public class AzureServiceBusTopicOperationAutoConfigurationTest {

    private static final String AZURE_PROPERTY_PREFIX = "spring.cloud.azure.";

    private static final AzureAmqpRetryOptionsConverter RETRY_OPTIONS_CONVERTER = new AzureAmqpRetryOptionsConverter();

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusTopicOperationAutoConfiguration.class));


    @Test
    public void testAzureServiceBusTopicDisabled() {
        this.contextRunner.withPropertyValues(AzureServiceBusProperties.PREFIX + ".enabled=false")
                          .run(context -> assertThat(context).doesNotHaveBean(ServiceBusTopicOperation.class));
    }

    @Test
    public void testWithoutAzureServiceBusTopicClient() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(ServiceBusProcessorClient.class))
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
        this.contextRunner.withUserConfiguration(TestConfigWithConnectionStringProvider.class,
            TestConfigWithServiceBusNamespaceManager.class)
                          .run(context -> assertThat(context).hasSingleBean(ServiceBusTopicClientFactory.class)
                                                             .hasSingleBean(ServiceBusTopicOperation.class));
    }

    @Test
    public void testConnectionStringProvided() {
        this.contextRunner.withPropertyValues(AzureServiceBusProperties.PREFIX + ".connection-string=" + NAMESPACE_CONNECTION_STRING)
                          .withUserConfiguration(AzureServiceBusOperationAutoConfiguration.class)
                          .run(context -> {
                              assertThat(context.getBean(ServiceBusConnectionStringProvider.class).getConnectionString()).isEqualTo(NAMESPACE_CONNECTION_STRING);
                              assertThat(context).doesNotHaveBean(ServiceBusNamespaceManager.class);
                              assertThat(context).doesNotHaveBean(ServiceBusTopicManager.class);
                              assertThat(context).doesNotHaveBean(ServiceBusTopicSubscriptionManager.class);
                              assertThat(context).hasSingleBean(ServiceBusTopicClientFactory.class);
                              assertThat(context).hasSingleBean(ServiceBusTopicOperation.class);
                              assertThat(context).hasSingleBean(ServiceBusMessageConverter.class);
                          });
    }

    @Test
    public void testTransportTypeWithAmqpWebSockets() {
        this.contextRunner.withPropertyValues(AzureServiceBusProperties.PREFIX + ".client.transport-type=AmqpWebSockets")
                          .withUserConfiguration(AzureServiceBusOperationAutoConfiguration.class)
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
                          .withUserConfiguration(AzureServiceBusOperationAutoConfiguration.class)
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
    public void testResourceManagerProvided() {
        this.contextRunner.withUserConfiguration(TestConfigWithAzureResourceManager.class,
                                                 TestConfigWithConnectionStringProvider.class,
                                                 AzureServiceBusOperationAutoConfiguration.class)
                          .withPropertyValues(
                              AZURE_PROPERTY_PREFIX + "resource-group=rg1",
                              AzureServiceBusProperties.PREFIX + "namespace=ns1"
                          )
                          .run(context -> {
                              assertThat(context).hasSingleBean(ServiceBusTopicClientFactory.class);
                              assertThat(context).hasSingleBean(ServiceBusTopicOperation.class);
                              assertThat(context).hasSingleBean(ServiceBusNamespaceManager.class);
                              assertThat(context).hasSingleBean(ServiceBusTopicManager.class);
                              assertThat(context).hasSingleBean(ServiceBusTopicSubscriptionManager.class);
                          });
    }

    @Test
    public void testMessageConverterProvided() {
        this.contextRunner.withUserConfiguration(
            TestConfigWithMessageConverter.class,
            AzureServiceBusOperationAutoConfiguration.class)
                          .withPropertyValues(
                              AzureServiceBusProperties.PREFIX + "connection-string=" + NAMESPACE_CONNECTION_STRING
                          )
                          .run(context -> {
                              assertThat(context).hasSingleBean(ServiceBusMessageConverter.class);
                              assertThat(context).hasSingleBean(ServiceBusTopicTemplate.class);

                              ServiceBusMessageConverter messageConverter =
                                  context.getBean(ServiceBusMessageConverter.class);
                              ServiceBusTopicTemplate topicTemplate = context.getBean(ServiceBusTopicTemplate.class);
                              assertSame(messageConverter, topicTemplate.getMessageConverter());
                          });
    }

    @Configuration
    @Import(TestConfigWithAzureResourceManager.class)
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
            return new ServiceBusConnectionStringProvider(NAMESPACE_CONNECTION_STRING);
        }

    }

    @Configuration
    @EnableConfigurationProperties(AzureContextProperties.class)
    public static class TestConfigWithMessageConverter {

        @Bean
        public ServiceBusMessageConverter messageConverter() {
            return mock(ServiceBusMessageConverter.class);
        }

    }
}
