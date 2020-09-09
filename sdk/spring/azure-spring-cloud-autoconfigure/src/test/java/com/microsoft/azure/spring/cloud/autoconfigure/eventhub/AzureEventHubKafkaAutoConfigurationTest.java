// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.eventhub;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.eventhub.EventHubAuthorizationKey;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import com.microsoft.azure.management.eventhub.EventHubNamespaceAuthorizationRule;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.rest.RestException;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AzureEventHubKafkaAutoConfigurationTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubKafkaAutoConfiguration.class))
        .withUserConfiguration(TestConfiguration.class);

    @Test
    public void testAzureEventHubDisabled() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubProperties.class));
    }

    @Test
    public void testWithoutKafkaTemplate() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(KafkaTemplate.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubProperties.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testAzureEventHubPropertiesNamespaceIllegal() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.namespace=")
            .withPropertyValues("spring.cloud.azure.eventhub.checkpoint-storage-account=")
            .run(context -> context.getBean(AzureEventHubProperties.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testAzureEventHubPropertiesStorageAccountIllegal() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.namespace=nsl")
            .withPropertyValues("spring.cloud.azure.eventhub.checkpoint-storage-account=1")
            .run(context -> context.getBean(AzureEventHubProperties.class));
    }

    @Ignore("org.apache.kafka.common.serialization.StringSerializer required on classpath")
    @Test
    public void testAzureEventHubPropertiesConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.namespace=ns1").run(context -> {
            assertThat(context).hasSingleBean(AzureEventHubProperties.class);
            assertThat(context.getBean(AzureEventHubProperties.class).getNamespace()).isEqualTo("ns1");
            assertThat(context).hasSingleBean(KafkaProperties.class);
            assertThat(context.getBean(KafkaProperties.class).getBootstrapServers().get(0)).isEqualTo("localhost:9093");
        });
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        ResourceManagerProvider resourceManagerProvider() {

            ResourceManagerProvider resourceManagerProvider = mock(ResourceManagerProvider.class);
            EventHubNamespace namespace = mock(EventHubNamespace.class);
            EventHubAuthorizationKey key = mock(EventHubAuthorizationKey.class);
            when(key.primaryConnectionString()).thenReturn("connectionString1");
            EventHubNamespaceAuthorizationRule rule = mock(EventHubNamespaceAuthorizationRule.class);
            when(rule.getKeys()).thenReturn(key);
            PagedList<EventHubNamespaceAuthorizationRule> rules = new PagedList<EventHubNamespaceAuthorizationRule>() {
                @Override
                public Page<EventHubNamespaceAuthorizationRule> nextPage(String nextPageLink)
                    throws RestException, IOException {
                    return null;
                }
            };
            rules.add(rule);
            when(namespace.listAuthorizationRules()).thenReturn(rules);
            when(namespace.serviceBusEndpoint()).thenReturn("localhost");
            return resourceManagerProvider;
        }

    }
}
