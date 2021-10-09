// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.spring.cloud.autoconfigure.eventhubs.kafka.AzureEventHubKafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AzureEventHubKafkaAutoConfigurationTest {

    private static final String EVENT_HUB_PROPERTY_PREFIX = "spring.cloud.azure.eventhubs.";
    private static final String AZURE_PROPERTY_PREFIX = "spring.cloud.azure.";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubKafkaAutoConfiguration.class));

    // TODO(xiada): add tests
/*
    @Test
    void testAzureEventHubDisabled() {
        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(AzureEventHubProperties.class));
    }

    @Test
    void testWithoutKafkaTemplate() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(KafkaTemplate.class))
                          .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubProperties.class));
    }

    @Test
    void testAzureEventHubPropertiesStorageAccountIllegal() {
        this.contextRunner.withPropertyValues(
            EVENT_HUB_PROPERTY_PREFIX + "namespace=ns1",
            EVENT_HUB_PROPERTY_PREFIX + "checkpoint-storage-account=1")
                          .run(context -> assertThrows(IllegalStateException.class,
                              () -> context.getBean(AzureEventHubProperties.class)));
    }

    @Test
    void testNamespaceProvided() {
        this.contextRunner.withPropertyValues(
            AZURE_PROPERTY_PREFIX + "resource-group=rg1",
            EVENT_HUB_PROPERTY_PREFIX + "namespace=ns1")
                          .withUserConfiguration(TestConfigurationWithResourceManager.class)
                          .run(context -> context.getBean(AzureEventHubProperties.class));
    }

    @Disabled("org.apache.kafka.common.serialization.StringSerializer required on classpath")
    @Test
    void testAzureEventHubPropertiesConfigured() {
        this.contextRunner.withPropertyValues(EVENT_HUB_PROPERTY_PREFIX + "namespace=ns1").run(context -> {
            assertThat(context).hasSingleBean(AzureEventHubProperties.class);
            assertThat(context.getBean(AzureEventHubProperties.class).getNamespace()).isEqualTo("ns1");
            assertThat(context).hasSingleBean(KafkaProperties.class);
            assertThat(context.getBean(KafkaProperties.class).getBootstrapServers().get(0)).isEqualTo("localhost:9093");
        });
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
    }*/

}
