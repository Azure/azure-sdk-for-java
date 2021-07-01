// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusQueueManager;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.ServiceBusConnectionStringProvider;
import com.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueTemplate;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AzureServiceBusQueueAutoConfigurationTest {

    private static final String SERVICE_BUS_PROPERTY_PREFIX = "spring.cloud.azure.servicebus.";
    private static final String AZURE_PROPERTY_PREFIX = "spring.cloud.azure.";

    private static final String NAMESPACE_NAME = "dummyNamespaceName";
    private static final String DEFAULT_DOMAIN_NAME = "servicebus.windows.net/";
    private static final String ENDPOINT_FORMAT = "sb://%s.%s";
    private static final String SHARED_ACCESS_KEY_NAME = "dummySasKeyName";
    private static final String SHARED_ACCESS_KEY = "dummySasKey";
    private static final String ENDPOINT = getUri(ENDPOINT_FORMAT, NAMESPACE_NAME, DEFAULT_DOMAIN_NAME).toString();
    static final String NAMESPACE_CONNECTION_STRING = String.format("Endpoint=%s;SharedAccessKeyName=%s;"
            + "SharedAccessKey=%s",
        ENDPOINT, SHARED_ACCESS_KEY_NAME, SHARED_ACCESS_KEY);
    private AutoCloseable closeable;

    @BeforeAll
    public void setup() {
        this.closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterAll
    public void close() throws Exception {
        this.closeable.close();
    }

    private static URI getUri(String endpointFormat, String namespace, String domainName) {
        try {
            return new URI(String.format(Locale.US, endpointFormat, namespace, domainName));
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "Invalid namespace name: %s", namespace), exception);
        }
    }

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusQueueAutoConfiguration.class));

    @Test
    public void testAzureServiceBusDisabled() {
        this.contextRunner.withPropertyValues(SERVICE_BUS_PROPERTY_PREFIX + "enabled=false")
                          .run(context -> assertThat(context).doesNotHaveBean(ServiceBusQueueOperation.class));
    }

    @Test
    public void testWithoutAzureServiceBusQueueClient() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(ServiceBusProcessorClient.class))
                          .run(context -> assertThat(context).doesNotHaveBean(ServiceBusQueueOperation.class));
    }

    @Test
    public void testWithoutServiceBusNamespaceManager() {
        this.contextRunner.withUserConfiguration(TestConfigWithConnectionStringProvider.class)
                          .run(context -> assertThat(context).doesNotHaveBean(ServiceBusQueueManager.class));
    }

    @Test
    public void testWithServiceBusNamespaceManager() {
        this.contextRunner.withUserConfiguration(TestConfigWithServiceBusNamespaceManager.class,
            TestConfigWithConnectionStringProvider.class)
                          .run(context -> assertThat(context).hasSingleBean(ServiceBusQueueManager.class));
    }

    @Test
    public void testQueueClientFactoryCreated() {
        this.contextRunner.withUserConfiguration(AzureServiceBusAutoConfiguration.class,
            TestConfigWithServiceBusNamespaceManager.class)
                          .withPropertyValues(SERVICE_BUS_PROPERTY_PREFIX + "connection-string=" + NAMESPACE_CONNECTION_STRING)
                          .run(context -> assertThat(context).hasSingleBean(ServiceBusQueueClientFactory.class)
                                                             .hasSingleBean(ServiceBusQueueOperation.class));
    }

    @Test
    public void testConnectionStringProvided() {
        this.contextRunner.withPropertyValues(SERVICE_BUS_PROPERTY_PREFIX + "connection-string=" + NAMESPACE_CONNECTION_STRING)
                          .withUserConfiguration(AzureServiceBusAutoConfiguration.class)
                          .run(context -> {
                              assertThat(context.getBean(ServiceBusConnectionStringProvider.class)
                                                .getConnectionString()).isEqualTo(NAMESPACE_CONNECTION_STRING);
                              assertThat(context).doesNotHaveBean(ServiceBusNamespaceManager.class);
                              assertThat(context).doesNotHaveBean(ServiceBusQueueManager.class);
                              assertThat(context).hasSingleBean(ServiceBusQueueClientFactory.class);
                              assertThat(context).hasSingleBean(ServiceBusQueueOperation.class);
                              assertThat(context).hasSingleBean(ServiceBusMessageConverter.class);
                          });
    }

    @Test
    public void testResourceManagerProvided() {
        this.contextRunner.withUserConfiguration(
            TestConfigWithAzureResourceManager.class,
            TestConfigWithConnectionStringProvider.class,
            AzureServiceBusAutoConfiguration.class)
                          .withPropertyValues(
                              AZURE_PROPERTY_PREFIX + "resource-group=rg1",
                              SERVICE_BUS_PROPERTY_PREFIX + "namespace=ns1"
                          )
                          .run(context -> {
                              assertThat(context).hasSingleBean(ServiceBusQueueClientFactory.class);
                              assertThat(context).hasSingleBean(ServiceBusQueueOperation.class);
                              assertThat(context).hasSingleBean(ServiceBusNamespaceManager.class);
                              assertThat(context).hasSingleBean(ServiceBusQueueManager.class);
                          });
    }

    @Test
    public void testMessageConverterProvided() {
        this.contextRunner.withUserConfiguration(
            TestConfigWithMessageConverter.class,
            AzureServiceBusAutoConfiguration.class)
                          .withPropertyValues(
                              SERVICE_BUS_PROPERTY_PREFIX + "connection-string" + NAMESPACE_CONNECTION_STRING
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
            return new ServiceBusConnectionStringProvider(NAMESPACE_CONNECTION_STRING);
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

    @Configuration
    @EnableConfigurationProperties(AzureProperties.class)
    public static class TestConfigWithMessageConverter {

        @Bean
        public ServiceBusMessageConverter messageConverter() {
            return mock(ServiceBusMessageConverter.class);
        }

    }


}
