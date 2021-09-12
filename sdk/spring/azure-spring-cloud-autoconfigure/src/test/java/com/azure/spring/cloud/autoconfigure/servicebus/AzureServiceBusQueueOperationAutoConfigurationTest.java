// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.spring.cloud.autoconfigure.commonconfig.TestConfigWithAzureResourceManager;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureServiceBusResourceManagerAutoConfiguration;
import com.azure.spring.core.StaticConnectionStringProvider;
import com.azure.spring.core.converter.AzureAmqpRetryOptionsConverter;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

public class AzureServiceBusQueueOperationAutoConfigurationTest {

    private static final String AZURE_PROPERTY_PREFIX = "spring.cloud.azure.";
    private static final String NAMESPACE_NAME = "dummyNamespaceName";
    private static final String DEFAULT_DOMAIN_NAME = "servicebus.windows.net/";
    private static final String ENDPOINT_FORMAT = "sb://%s.%s";
    private static final String SHARED_ACCESS_KEY_NAME = "dummySasKeyName";
    private static final String SHARED_ACCESS_KEY = "dummySasKey";
    private static final String ENDPOINT = getUri(ENDPOINT_FORMAT, NAMESPACE_NAME, DEFAULT_DOMAIN_NAME).toString();
    static final String NAMESPACE_CONNECTION_STRING = String.format(
        "Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s", ENDPOINT, SHARED_ACCESS_KEY_NAME, SHARED_ACCESS_KEY);

    private static final AzureAmqpRetryOptionsConverter RETRY_OPTIONS_CONVERTER = new AzureAmqpRetryOptionsConverter();

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusQueueOperationAutoConfiguration.class));

    private static URI getUri(String endpointFormat, String namespace, String domainName) {
        try {
            return new URI(String.format(Locale.US, endpointFormat, namespace, domainName));
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "Invalid namespace name: %s", namespace), exception);
        }
    }

    @Test
    public void testAzureServiceBusDisabled() {
        this.contextRunner.withPropertyValues(AzureServiceBusProperties.PREFIX + ".enabled=false")
                          .run(context -> assertThat(context).doesNotHaveBean(ServiceBusQueueOperation.class));
    }

    @Test
    public void testWithoutAzureServiceBusQueueClient() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(ServiceBusQueueClientFactory.class))
                          .run(context -> assertThat(context).doesNotHaveBean(ServiceBusQueueOperation.class));
    }

    @Test
    public void testQueueClientFactoryCreated() {
        this.contextRunner.withUserConfiguration()
                          .withPropertyValues(AzureServiceBusProperties.PREFIX + ".connection-string=" + NAMESPACE_CONNECTION_STRING)
                          .run(context -> assertThat(context).hasSingleBean(ServiceBusQueueClientFactory.class)
                                                             .hasSingleBean(ServiceBusQueueOperation.class));
    }

    @Test
    public void testConnectionStringProvided() {
        this.contextRunner.withPropertyValues(AzureServiceBusProperties.PREFIX + ".connection-string=" + NAMESPACE_CONNECTION_STRING)
                          .withUserConfiguration(AzureServiceBusResourceManagerAutoConfiguration.class)
                          .run(context -> {
                              assertThat(context).hasSingleBean(ServiceBusQueueClientFactory.class);
                              assertThat(context).hasSingleBean(ServiceBusQueueOperation.class);
                              assertThat(context).hasSingleBean(ServiceBusMessageConverter.class);
                          });
    }

    @Test
    public void testTransportTypeWithAmqpWebSockets() {
        this.contextRunner.withPropertyValues(AzureServiceBusProperties.PREFIX + ".client.transport-type=AmqpWebSockets")
                          .withUserConfiguration(AzureServiceBusResourceManagerAutoConfiguration.class)
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
                          .withUserConfiguration(AzureServiceBusResourceManagerAutoConfiguration.class)
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
        this.contextRunner.withUserConfiguration(
            TestConfigWithAzureResourceManager.class,
            StaticConnectionStringProviderConfiguration.class,
            AzureServiceBusResourceManagerAutoConfiguration.class)
                          .withPropertyValues(
                              AZURE_PROPERTY_PREFIX + "resource-group=rg1",
                              AzureServiceBusProperties.PREFIX + "namespace=ns1"
                          )
                          .run(context -> {
                              assertThat(context).hasSingleBean(ServiceBusQueueClientFactory.class);
                              assertThat(context).hasSingleBean(ServiceBusQueueOperation.class);
                          });
    }

    @Test
    public void testMessageConverterProvided() {
        this.contextRunner.withUserConfiguration(
            MessageConverterConfiguration.class,
            AzureServiceBusResourceManagerAutoConfiguration.class)
                          .withPropertyValues(
                              AzureServiceBusProperties.PREFIX + "connection-string" + NAMESPACE_CONNECTION_STRING
                          )
                          .run(context -> {
                              assertThat(context).hasSingleBean(ServiceBusMessageConverter.class);
                              assertThat(context).hasSingleBean(ServiceBusQueueTemplate.class);

                              ServiceBusMessageConverter messageConverter =
                                  context.getBean(ServiceBusMessageConverter.class);
                              ServiceBusQueueTemplate queueTemplate = context.getBean(ServiceBusQueueTemplate.class);
                              assertSame(messageConverter, queueTemplate.getMessageConverter());
                          });
    }


    @Configuration(proxyBeanMethods = false)
    static class StaticConnectionStringProviderConfiguration {

        @Bean
        public StaticConnectionStringProvider<AzureServiceType.ServiceBus> serviceBusConnectionStringProvider() {
            return new StaticConnectionStringProvider<>(AzureServiceType.SERVICE_BUS, NAMESPACE_CONNECTION_STRING);
        }

    }

    @Configuration
    static class MessageConverterConfiguration {

        @Bean
        public ServiceBusMessageConverter messageConverter() {
            return mock(ServiceBusMessageConverter.class);
        }

    }


}
