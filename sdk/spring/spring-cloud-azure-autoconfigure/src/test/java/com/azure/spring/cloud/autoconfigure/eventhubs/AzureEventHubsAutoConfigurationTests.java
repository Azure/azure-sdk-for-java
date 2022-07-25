// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.AbstractAzureServiceConfigurationTests;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.core.provider.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.cloud.service.implementation.eventhubs.factory.EventHubClientBuilderFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;
import java.time.Instant;

import static com.azure.messaging.eventhubs.LoadBalancingStrategy.GREEDY;
import static com.azure.spring.cloud.autoconfigure.eventhubs.EventHubsTestUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzureEventHubsAutoConfigurationTests extends AbstractAzureServiceConfigurationTests<
    EventHubClientBuilderFactory, AzureEventHubsProperties> {
    private static final String CONNECTION_STRING = String.format(CONNECTION_STRING_FORMAT, "test-namespace");
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubsAutoConfiguration.class));

    @Override
    protected ApplicationContextRunner getMinimalContextRunner() {
        return this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=test-eventhub-namespace",
                "spring.cloud.azure.eventhubs.event-hub-name=test-eventhub"
                );
    }

    @Override
    protected String getPropertyPrefix() {
        return AzureEventHubsProperties.PREFIX;
    }

    @Override
    protected Class<EventHubClientBuilderFactory> getBuilderFactoryType() {
        return EventHubClientBuilderFactory.class;
    }

    @Override
    protected Class<AzureEventHubsProperties> getConfigurationPropertiesType() {
        return AzureEventHubsProperties.class;
    }

    @Test
    void configureWithoutEventHubClientBuilder() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(EventHubClientBuilder.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsAutoConfiguration.class));
    }

    @Test
    void configureWithEventHubDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsAutoConfiguration.class));
    }

    @Test
    void configureWithoutConnectionStringAndNamespace() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.enabled=true")
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsAutoConfiguration.class));
    }

    @Test
    void configureWithNamespace() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.namespace=test-eventhub-namespace")
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> assertThat(context).hasSingleBean(AzureEventHubsProperties.class));
    }

    @Test
    void configureWithConnectionString() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.connection-string=" + CONNECTION_STRING)
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> assertThat(context).hasSingleBean(AzureEventHubsProperties.class));
    }

    @Test
    void configureAzureEventHubsPropertiesWithGlobalDefaults() {
        AzureGlobalProperties azureProperties = new AzureGlobalProperties();
        azureProperties.getCredential().setClientId("azure-client-id");
        azureProperties.getCredential().setClientSecret("azure-client-secret");
        azureProperties.getRetry().getExponential().setBaseDelay(Duration.ofSeconds(2));
        azureProperties.getRetry().getFixed().setDelay(Duration.ofSeconds(3));

        this.contextRunner
            .withBean(AzureGlobalProperties.class, () -> azureProperties)
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.credential.client-id=eventhubs-client-id",
                "spring.cloud.azure.eventhubs.retry.exponential.base-delay=2m",
                "spring.cloud.azure.eventhubs.connection-string=" + CONNECTION_STRING
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsProperties.class);
                final AzureEventHubsProperties properties = context.getBean(AzureEventHubsProperties.class);
                assertThat(properties.getCredential().getClientId()).isEqualTo("eventhubs-client-id");
                assertThat(properties.getCredential().getClientSecret()).isEqualTo("azure-client-secret");
                assertThat(properties.getRetry().getExponential().getBaseDelay()).isEqualTo(Duration.ofMinutes(2));
                assertThat(properties.getRetry().getFixed().getDelay()).isEqualTo(Duration.ofSeconds(3));
                assertThat(properties.getConnectionString()).isEqualTo(CONNECTION_STRING);

                assertThat(azureProperties.getCredential().getClientId()).isEqualTo("azure-client-id");
            });
    }

    @Test
    @SuppressWarnings("rawtypes")
    void connectionStringProvidedShouldConfigureConnectionProvider() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.cloud.azure.eventhubs.event-hub-name=test-event-hub"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsAutoConfiguration.class);
                assertThat(context).hasSingleBean(StaticConnectionStringProvider.class);
                StaticConnectionStringProvider connectionStringProvider = context.getBean(StaticConnectionStringProvider.class);
                Assertions.assertEquals(AzureServiceType.EVENT_HUBS, connectionStringProvider.getServiceType());
            });
    }

    @Test
    void configurationPropertiesShouldBind() {
        String connectionString = String.format(CONNECTION_STRING_FORMAT, "fake-namespace");
        String producerConnectionString = String.format(CONNECTION_STRING_FORMAT, "fake-producer-namespace");
        String consumerConnectionString = String.format(CONNECTION_STRING_FORMAT, "fake-consumer-namespace");
        String processorConnectionString = String.format(CONNECTION_STRING_FORMAT, "fake-processor-namespace");
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.credential.client-id=eventhubs-client-id",
                
                "spring.cloud.azure.eventhubs.shared-connection=true",
                "spring.cloud.azure.eventhubs.domain-name=fake-domain",
                "spring.cloud.azure.eventhubs.namespace=fake-namespace",
                "spring.cloud.azure.eventhubs.event-hub-name=fake-event-hub",
                "spring.cloud.azure.eventhubs.connection-string=" + connectionString,
                "spring.cloud.azure.eventhubs.custom-endpoint-address=http://fake-custom-endpoint.com",

                "spring.cloud.azure.eventhubs.producer.domain-name=fake-producer-domain",
                "spring.cloud.azure.eventhubs.producer.namespace=fake-producer-namespace",
                "spring.cloud.azure.eventhubs.producer.event-hub-name=fake-producer-event-hub",
                "spring.cloud.azure.eventhubs.producer.connection-string=" + producerConnectionString,
                "spring.cloud.azure.eventhubs.producer.custom-endpoint-address=http://fake-producer-custom-endpoint.com",

                "spring.cloud.azure.eventhubs.consumer.domain-name=fake-consumer-domain",
                "spring.cloud.azure.eventhubs.consumer.namespace=fake-consumer-namespace",
                "spring.cloud.azure.eventhubs.consumer.event-hub-name=fake-consumer-event-hub",
                "spring.cloud.azure.eventhubs.consumer.connection-string=" + consumerConnectionString,
                "spring.cloud.azure.eventhubs.consumer.custom-endpoint-address=http://fake-consumer-custom-endpoint.com",
                "spring.cloud.azure.eventhubs.consumer.consumer-group=fake-consumer-consumer-group",
                "spring.cloud.azure.eventhubs.consumer.prefetch-count=1",

                "spring.cloud.azure.eventhubs.processor.domain-name=fake-processor-domain",
                "spring.cloud.azure.eventhubs.processor.namespace=fake-processor-namespace",
                "spring.cloud.azure.eventhubs.processor.event-hub-name=fake-processor-event-hub",
                "spring.cloud.azure.eventhubs.processor.connection-string=" + processorConnectionString,
                "spring.cloud.azure.eventhubs.processor.custom-endpoint-address=http://fake-processor-custom-endpoint.com",
                "spring.cloud.azure.eventhubs.processor.consumer-group=fake-processor-consumer-group",
                "spring.cloud.azure.eventhubs.processor.prefetch-count=2",
                "spring.cloud.azure.eventhubs.processor.track-last-enqueued-event-properties=true",
                "spring.cloud.azure.eventhubs.processor.initial-partition-event-position.0.offset=earliest",
                "spring.cloud.azure.eventhubs.processor.initial-partition-event-position.1.enqueued-date-time=2022-01-01T10:10:00Z",
                "spring.cloud.azure.eventhubs.processor.initial-partition-event-position.2.sequence-number=1000",
                "spring.cloud.azure.eventhubs.processor.initial-partition-event-position.2.inclusive=true",
                "spring.cloud.azure.eventhubs.processor.batch.max-wait-time=5s",
                "spring.cloud.azure.eventhubs.processor.batch.max-size=8",
                "spring.cloud.azure.eventhubs.processor.load-balancing.update-interval=7m",
                "spring.cloud.azure.eventhubs.processor.load-balancing.strategy=greedy",
                "spring.cloud.azure.eventhubs.processor.load-balancing.partition-ownership-expiration-interval=2h",
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.create-container-if-not-exists=true"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsProperties.class);
                AzureEventHubsProperties properties = context.getBean(AzureEventHubsProperties.class);
                
                assertTrue(properties.getSharedConnection());
                assertEquals("fake-domain", properties.getDomainName());
                assertEquals("fake-namespace", properties.getNamespace());
                assertEquals("fake-event-hub", properties.getEventHubName());
                assertEquals(connectionString, properties.getConnectionString());
                assertEquals("http://fake-custom-endpoint.com", properties.getCustomEndpointAddress());

                AzureEventHubsProperties.Producer producer = properties.getProducer();
                assertEquals("fake-producer-domain", producer.getDomainName());
                assertEquals("fake-producer-namespace", producer.getNamespace());
                assertEquals("fake-producer-event-hub", producer.getEventHubName());
                assertEquals(producerConnectionString, producer.getConnectionString());
                assertEquals("http://fake-producer-custom-endpoint.com", producer.getCustomEndpointAddress());

                AzureEventHubsProperties.Consumer consumer = properties.getConsumer();
                assertEquals("fake-consumer-domain", consumer.getDomainName());
                assertEquals("fake-consumer-namespace", consumer.getNamespace());
                assertEquals("fake-consumer-event-hub", consumer.getEventHubName());
                assertEquals(consumerConnectionString, consumer.getConnectionString());
                assertEquals("http://fake-consumer-custom-endpoint.com", consumer.getCustomEndpointAddress());
                assertEquals("fake-consumer-consumer-group", consumer.getConsumerGroup());
                assertEquals(1, consumer.getPrefetchCount());

                AzureEventHubsProperties.Processor processor  = properties.getProcessor();
                assertEquals("fake-processor-domain", processor.getDomainName());
                assertEquals("fake-processor-namespace", processor.getNamespace());
                assertEquals("fake-processor-event-hub", processor.getEventHubName());
                assertEquals(processorConnectionString, processor.getConnectionString());
                assertEquals("http://fake-processor-custom-endpoint.com", processor.getCustomEndpointAddress());
                assertEquals("fake-processor-consumer-group", processor.getConsumerGroup());
                assertEquals(2, processor.getPrefetchCount());
                assertTrue(processor.getTrackLastEnqueuedEventProperties());
                assertEquals("earliest", processor.getInitialPartitionEventPosition().get("0").getOffset());
                assertEquals(Instant.parse("2022-01-01T10:10:00Z"), processor.getInitialPartitionEventPosition().get("1").getEnqueuedDateTime());
                assertEquals(1000, processor.getInitialPartitionEventPosition().get("2").getSequenceNumber());
                assertTrue(processor.getInitialPartitionEventPosition().get("2").isInclusive());
                assertEquals(Duration.ofSeconds(5), processor.getBatch().getMaxWaitTime());
                assertEquals(8, processor.getBatch().getMaxSize());
                assertEquals(Duration.ofMinutes(7), processor.getLoadBalancing().getUpdateInterval());
                assertEquals(GREEDY, processor.getLoadBalancing().getStrategy());
                assertEquals(Duration.ofHours(2), processor.getLoadBalancing().getPartitionOwnershipExpirationInterval());
                assertTrue(processor.getCheckpointStore().isCreateContainerIfNotExists());

            });
    }

}
