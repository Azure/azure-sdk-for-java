// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.cloud.autoconfigure.commonconfig.TestConfigWithAzureResourceManager;
import com.azure.spring.cloud.autoconfigure.context.AzureContextProperties;
import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusTopicManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusTopicSubscriptionManager;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import com.azure.spring.servicebus.core.ServiceBusTopicClientFactory;
import com.azure.spring.servicebus.core.topic.ServiceBusTopicOperation;
import com.azure.spring.servicebus.core.topic.ServiceBusTopicTemplate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusQueueAutoConfigurationTest.NAMESPACE_CONNECTION_STRING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AzureServiceBusTopicAutoConfigurationTest {

    private static final String SERVICE_BUS_PROPERTY_PREFIX = "spring.cloud.azure.servicebus.";
    private static final String AZURE_PROPERTY_PREFIX = "spring.cloud.azure.";

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusTopicAutoConfiguration.class));
    private AutoCloseable closeable;

    @BeforeAll
    public void setup() {
        this.closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterAll
    public void close() throws Exception {
        this.closeable.close();
    }

    @Test
    public void testAzureServiceBusTopicDisabled() {
        this.contextRunner.withPropertyValues(SERVICE_BUS_PROPERTY_PREFIX + "enabled=false")
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
        this.contextRunner.withPropertyValues(SERVICE_BUS_PROPERTY_PREFIX + "connection-string=" + NAMESPACE_CONNECTION_STRING)
                          .withUserConfiguration(AzureServiceBusAutoConfiguration.class)
                          .run(context -> {
                              assertThat(context.getBean(ServiceBusConnectionStringProvider.class).getConnectionString()).isEqualTo(NAMESPACE_CONNECTION_STRING);
                              assertThat(context.getBean(AzureServiceBusProperties.class).getTransportType()).isEqualTo(AmqpTransportType.AMQP);
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
        this.contextRunner.withPropertyValues(SERVICE_BUS_PROPERTY_PREFIX + "transport-type=AMQP_WEB_SOCKETS")
                          .withUserConfiguration(AzureServiceBusAutoConfiguration.class)
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
                          .withUserConfiguration(AzureServiceBusAutoConfiguration.class)
                          .run(context -> {
                              assertThat(context.getBean(AzureServiceBusProperties.class).getRetryOptions().getMaxRetries()).isEqualTo(5);
                              assertThat(context.getBean(AzureServiceBusProperties.class).getRetryOptions().getDelay().getSeconds()).isEqualTo(100L);
                              assertThat(context.getBean(AzureServiceBusProperties.class).getRetryOptions().getMaxDelay().getSeconds()).isEqualTo(200L);
                              assertThat(context.getBean(AzureServiceBusProperties.class).getRetryOptions().getTryTimeout().getSeconds()).isEqualTo(300L);
                              assertThat(context.getBean(AzureServiceBusProperties.class).getRetryOptions().getMode()).isEqualTo(AmqpRetryMode.FIXED);
                          });
    }

    @Test
    public void testResourceManagerProvided() {
        this.contextRunner.withUserConfiguration(TestConfigWithAzureResourceManager.class,
                                                 TestConfigWithConnectionStringProvider.class, AzureServiceBusAutoConfiguration.class)
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

    @Test
    public void testMessageConverterProvided() {
        this.contextRunner.withUserConfiguration(
            TestConfigWithMessageConverter.class,
            AzureServiceBusAutoConfiguration.class)
                          .withPropertyValues(
                              SERVICE_BUS_PROPERTY_PREFIX + "connection-string=" + NAMESPACE_CONNECTION_STRING
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
