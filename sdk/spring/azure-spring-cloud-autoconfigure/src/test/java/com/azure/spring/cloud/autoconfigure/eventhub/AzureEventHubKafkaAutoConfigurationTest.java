// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.IterableStream;
import com.azure.resourcemanager.eventhubs.models.EventHubAuthorizationKey;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceAuthorizationRule;
import com.azure.spring.cloud.context.core.impl.EventHubNamespaceManager;
import com.google.common.collect.Lists;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Mono;

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
        EventHubNamespaceManager eventHubNamespaceManager() {

            EventHubNamespace namespace = mock(EventHubNamespace.class);
            EventHubAuthorizationKey key = mock(EventHubAuthorizationKey.class);
            when(key.primaryConnectionString()).thenReturn("connectionString1");
            EventHubNamespaceAuthorizationRule rule = mock(EventHubNamespaceAuthorizationRule.class);
            when(rule.getKeys()).thenReturn(key);

            PagedIterable<EventHubNamespaceAuthorizationRule> rules =
                new PagedIterable<>(new PagedFlux<>(
                    () -> Mono.just(new PagedResponseBase<String, EventHubNamespaceAuthorizationRule>(
                        null, 200, null, new Page<EventHubNamespaceAuthorizationRule>() {
                        @Override
                        public IterableStream<EventHubNamespaceAuthorizationRule> getElements() {
                            return new IterableStream<>(Lists.newArrayList(rule));
                        }

                        @Override
                        public String getContinuationToken() {
                            return null;
                        }
                    }, null))
                ));

            when(namespace.listAuthorizationRules()).thenReturn(rules);
            when(namespace.serviceBusEndpoint()).thenReturn("localhost");
            //This previously returned a ResourceManagerProvider that was in no way connected to any of the objets created above.
            //Maintaining similar behavior in refactoring.
            return mock(EventHubNamespaceManager.class);
        }

    }
}
