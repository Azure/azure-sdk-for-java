// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.IterableStream;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.eventhubs.models.EventHubAuthorizationKey;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceAuthorizationRule;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaces;
import com.azure.spring.cloud.autoconfigure.commonconfig.TestConfigWithAzureResourceManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Mono;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AzureEventHubKafkaAutoConfigurationTest {

    private static final String EVENT_HUB_PROPERTY_PREFIX = "spring.cloud.azure.eventhub.";
    private static final String AZURE_PROPERTY_PREFIX = "spring.cloud.azure.";

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubKafkaAutoConfiguration.class));

    @Test
    public void testAzureEventHubDisabled() {
        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(AzureEventHubProperties.class));
    }

    @Test
    public void testWithoutKafkaTemplate() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(KafkaTemplate.class))
                          .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubProperties.class));
    }

    @Test
    public void testAzureEventHubPropertiesStorageAccountIllegal() {
        this.contextRunner.withPropertyValues(
            EVENT_HUB_PROPERTY_PREFIX + "namespace=ns1",
            EVENT_HUB_PROPERTY_PREFIX + "checkpoint-storage-account=1")
                          .run(context -> assertThrows(IllegalStateException.class,
                              () -> context.getBean(AzureEventHubProperties.class)));
    }

    @Test
    public void testNamespaceProvided() {
        this.contextRunner.withPropertyValues(
            AZURE_PROPERTY_PREFIX + "resource-group=rg1",
            EVENT_HUB_PROPERTY_PREFIX + "namespace=ns1")
                          .withUserConfiguration(TestConfigurationWithResourceManager.class)
                          .run(context -> context.getBean(AzureEventHubProperties.class));
    }

    @Disabled("org.apache.kafka.common.serialization.StringSerializer required on classpath")
    @Test
    public void testAzureEventHubPropertiesConfigured() {
        this.contextRunner.withPropertyValues(EVENT_HUB_PROPERTY_PREFIX + "namespace=ns1").run(context -> {
            assertThat(context).hasSingleBean(AzureEventHubProperties.class);
            assertThat(context.getBean(AzureEventHubProperties.class).getNamespace()).isEqualTo("ns1");
            assertThat(context).hasSingleBean(KafkaProperties.class);
            assertThat(context.getBean(KafkaProperties.class).getBootstrapServers().get(0)).isEqualTo("localhost:9093");
        });
    }

    @Configuration
    @Import(TestConfigWithAzureResourceManager.class)
    public static class TestConfigurationWithResourceManager {

        @Bean
        @Primary
        public AzureResourceManager azureResourceManagerMock() {
            final AzureResourceManager mockResourceManager = mock(AzureResourceManager.class);
            final EventHubNamespaces mockNamespaces = mock(EventHubNamespaces.class);
            final EventHubNamespace mockNamespace = mock(EventHubNamespace.class);
            final EventHubNamespaceAuthorizationRule mockRule = mock(EventHubNamespaceAuthorizationRule.class);
            final EventHubAuthorizationKey mockAuthorizationKey = mock(EventHubAuthorizationKey.class);

            when(mockResourceManager.eventHubNamespaces()).thenReturn(mockNamespaces);
            when(mockNamespaces.getByResourceGroup(anyString(), anyString())).thenReturn(mockNamespace);
            when(mockNamespace.listAuthorizationRules()).thenReturn(buildPagedIterable(mockRule));
            when(mockRule.getKeys()).thenReturn(mockAuthorizationKey);
            when(mockAuthorizationKey.primaryConnectionString()).thenReturn("str1");
            when(mockNamespace.serviceBusEndpoint()).thenReturn("https://localhost:8080/");

            return mockResourceManager;
        }
    }

    static <T> PagedIterable<T> buildPagedIterable(T element) {
        return new PagedIterable<>(new PagedFlux<>(() -> Mono.just(
            new PagedResponseBase<String, T>(null, 200, null, new Page<T>() {

                @Override
                public IterableStream<T> getElements() {
                    return new IterableStream<>(singletonList(element));
                }

                @Override
                public String getContinuationToken() {
                    return null;
                }
            }, null))
        ));
    }

}
