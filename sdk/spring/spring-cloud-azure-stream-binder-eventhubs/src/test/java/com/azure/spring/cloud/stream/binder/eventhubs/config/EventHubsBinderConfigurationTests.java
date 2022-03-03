// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.config;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.EventHubsMessageChannelBinder;
import com.azure.spring.cloud.stream.binder.eventhubs.EventHubsMessageChannelTestBinder;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsConsumerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsProducerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubsChannelProvisioner;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubsChannelResourceManagerProvisioner;
import com.azure.spring.eventhubs.core.EventHubsProcessorFactory;
import com.azure.spring.eventhubs.core.listener.EventHubsMessageListenerContainer;
import com.azure.spring.eventhubs.core.properties.EventHubsContainerProperties;
import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.eventhubs.core.properties.ProcessorProperties;
import com.azure.spring.eventhubs.implementation.core.DefaultEventHubsNamespaceProcessorFactory;
import com.azure.spring.integration.eventhubs.inbound.EventHubsInboundChannelAdapter;
import com.azure.spring.messaging.ConsumerIdentifier;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.resourcemanager.provisioning.EventHubsProvisioner;
import com.azure.spring.service.eventhubs.consumer.EventHubsErrorHandler;
import com.azure.spring.service.eventhubs.consumer.EventHubsMessageListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;

import static com.azure.messaging.eventhubs.LoadBalancingStrategy.GREEDY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class EventHubsBinderConfigurationTests {

    private static final String CONNECTION_STRING_FORMAT =
        "Endpoint=sb://%s.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key";
    
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(EventHubsBinderConfiguration.class));

    @Test
    void configurationNotMatchedWhenBinderBeanExist() {
        this.contextRunner
            .withBean(Binder.class, () -> mock(Binder.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean(EventHubsBinderConfiguration.class);
                assertThat(context).doesNotHaveBean(EventHubsMessageChannelBinder.class);
            });
    }

    @Test
    void shouldConfigureDefaultChannelProvisionerWhenNoResourceManagerProvided() {
        this.contextRunner
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubsBinderConfiguration.class);
                assertThat(context).hasSingleBean(EventHubsExtendedBindingProperties.class);
                assertThat(context).hasSingleBean(EventHubsChannelProvisioner.class);
                assertThat(context).hasSingleBean(EventHubsMessageChannelBinder.class);

                EventHubsChannelProvisioner channelProvisioner = context.getBean(EventHubsChannelProvisioner.class);
                assertThat(channelProvisioner).isNotInstanceOf(EventHubsChannelResourceManagerProvisioner.class);
            });
    }

    @Test
    void shouldConfigureArmChannelProvisionerWhenResourceManagerProvided() {
        AzureEventHubsProperties properties = new AzureEventHubsProperties();
        properties.setNamespace("test");
        this.contextRunner
            .withBean(EventHubsProvisioner.class, () -> mock(EventHubsProvisioner.class))
            .withBean(AzureEventHubsProperties.class, () -> properties)
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubsBinderConfiguration.class);
                assertThat(context).hasSingleBean(EventHubsExtendedBindingProperties.class);
                assertThat(context).hasSingleBean(EventHubsChannelProvisioner.class);
                assertThat(context).hasSingleBean(EventHubsMessageChannelBinder.class);

                EventHubsChannelProvisioner channelProvisioner = context.getBean(EventHubsChannelProvisioner.class);
                assertThat(channelProvisioner).isInstanceOf(EventHubsChannelResourceManagerProvisioner.class);
            });
    }

    @Test
    void shouldConfigureConsumerPrefetchCount() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TestProcessorContainerConfiguration.class))
            .withPropertyValues(
                "spring.cloud.stream.eventhubs.bindings.consume-in-0.consumer.prefetch-count=150",
                "spring.cloud.stream.eventhubs.bindings.consume-in-0.consumer.destination=dest",
                "spring.cloud.stream.eventhubs.bindings.consume-in-0.consumer.group=group",
                "spring.cloud.stream.eventhubs.namespace=namespace"
                )
            .run(context -> {
                EventHubsExtendedBindingProperties properties = context.getBean(EventHubsExtendedBindingProperties.class);
                EventHubsConsumerProperties consumerProperties = properties.getExtendedConsumerProperties("consume-in-0");
                assertThat(consumerProperties.getPrefetchCount()).isEqualTo(150);
            });
    }

    @Test
    void testExtendedBindingPropertiesShouldBind() {
        String producerConnectionString = String.format(CONNECTION_STRING_FORMAT, "fake-producer-namespace");
        String consumerConnectionString = String.format(CONNECTION_STRING_FORMAT, "fake-consumer-namespace");

        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(EventHubsExtendedBindingPropertiesTestConfiguration.class))
            .withPropertyValues(
                "spring.cloud.stream.eventhubs.bindings.input.consumer.domain-name=fake-consumer-domain",
                "spring.cloud.stream.eventhubs.bindings.input.consumer.namespace=fake-consumer-namespace",
                "spring.cloud.stream.eventhubs.bindings.input.consumer.connection-string=" + consumerConnectionString,
                "spring.cloud.stream.eventhubs.bindings.input.consumer.custom-endpoint-address=http://fake-consumer-custom-endpoint.com",
                "spring.cloud.stream.eventhubs.bindings.input.consumer.prefetch-count=1",
                "spring.cloud.stream.eventhubs.bindings.input.consumer.track-last-enqueued-event-properties=true",
                "spring.cloud.stream.eventhubs.bindings.input.consumer.initial-partition-event-position.0.offset=earliest",
                "spring.cloud.stream.eventhubs.bindings.input.consumer.initial-partition-event-position.1.enqueued-date-time=2022-01-01T10:10:00Z",
                "spring.cloud.stream.eventhubs.bindings.input.consumer.initial-partition-event-position.2.sequence-number=1000",
                "spring.cloud.stream.eventhubs.bindings.input.consumer.initial-partition-event-position.2.inclusive=true",
                "spring.cloud.stream.eventhubs.bindings.input.consumer.batch.max-wait-time=5s",
                "spring.cloud.stream.eventhubs.bindings.input.consumer.batch.max-size=8",
                "spring.cloud.stream.eventhubs.bindings.input.consumer.load-balancing.update-interval=7m",
                "spring.cloud.stream.eventhubs.bindings.input.consumer.load-balancing.strategy=greedy",
                "spring.cloud.stream.eventhubs.bindings.input.consumer.load-balancing.partition-ownership-expiration-interval=2h",
                "spring.cloud.stream.eventhubs.bindings.input.consumer.checkpoint.mode=BATCH",
                "spring.cloud.stream.eventhubs.bindings.input.consumer.checkpoint.count=10",
                "spring.cloud.stream.eventhubs.bindings.input.consumer.checkpoint.interval=10s",

                "spring.cloud.stream.eventhubs.bindings.input.producer.domain-name=fake-producer-domain",
                "spring.cloud.stream.eventhubs.bindings.input.producer.namespace=fake-producer-namespace",
                "spring.cloud.stream.eventhubs.bindings.input.producer.connection-string=" + producerConnectionString,
                "spring.cloud.stream.eventhubs.bindings.input.producer.custom-endpoint-address=http://fake-producer-custom-endpoint.com",
                "spring.cloud.stream.eventhubs.bindings.input.producer.sync=true",
                "spring.cloud.stream.eventhubs.bindings.input.producer.send-timeout=5m"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubsExtendedBindingProperties.class);
                EventHubsExtendedBindingProperties extendedBindingProperties =
                    context.getBean(EventHubsExtendedBindingProperties.class);

                assertThat(extendedBindingProperties.getExtendedConsumerProperties("input")).isNotNull();

                EventHubsConsumerProperties consumerProperties =
                    extendedBindingProperties.getExtendedConsumerProperties("input");
                assertEquals("fake-consumer-domain", consumerProperties.getDomainName());
                assertEquals("fake-consumer-namespace", consumerProperties.getNamespace());
                assertEquals(consumerConnectionString, consumerProperties.getConnectionString());
                assertEquals("http://fake-consumer-custom-endpoint.com", consumerProperties.getCustomEndpointAddress());
                assertEquals(1, consumerProperties.getPrefetchCount());
                assertTrue(consumerProperties.getTrackLastEnqueuedEventProperties());
                assertEquals("earliest", consumerProperties.getInitialPartitionEventPosition().get("0").getOffset());
                assertEquals(Instant.parse("2022-01-01T10:10:00Z"), consumerProperties.getInitialPartitionEventPosition().get("1").getEnqueuedDateTime());
                assertEquals(1000, consumerProperties.getInitialPartitionEventPosition().get("2").getSequenceNumber());
                assertTrue(consumerProperties.getInitialPartitionEventPosition().get("2").isInclusive());
                assertEquals(Duration.ofSeconds(5), consumerProperties.getBatch().getMaxWaitTime());
                assertEquals(8, consumerProperties.getBatch().getMaxSize());
                assertEquals(Duration.ofMinutes(7), consumerProperties.getLoadBalancing().getUpdateInterval());
                assertEquals(GREEDY, consumerProperties.getLoadBalancing().getStrategy());
                assertEquals(Duration.ofHours(2), consumerProperties.getLoadBalancing().getPartitionOwnershipExpirationInterval());
                assertEquals(CheckpointMode.BATCH, consumerProperties.getCheckpoint().getMode());
                assertEquals(Duration.ofSeconds(10), consumerProperties.getCheckpoint().getInterval());
                assertEquals(10, consumerProperties.getCheckpoint().getCount());

                EventHubsProducerProperties producerProperties =
                    extendedBindingProperties.getExtendedProducerProperties("input");
                assertEquals("fake-producer-domain", producerProperties.getDomainName());
                assertEquals("fake-producer-namespace", producerProperties.getNamespace());
                assertEquals(producerConnectionString, producerProperties.getConnectionString());
                assertEquals("http://fake-producer-custom-endpoint.com", producerProperties.getCustomEndpointAddress());
                assertTrue(producerProperties.isSync());
                assertEquals(Duration.ofMinutes(5), producerProperties.getSendTimeout());
            });
    }

    @Configuration
    @EnableConfigurationProperties(EventHubsExtendedBindingProperties.class)
    static class TestProcessorContainerConfiguration {

        @Bean
        public EventHubsMessageChannelTestBinder eventHubBinder(EventHubsExtendedBindingProperties bindingProperties,
                                                                ObjectProvider<NamespaceProperties> namespaceProperties,
                                                                ObjectProvider<CheckpointStore> checkpointStores) {

            EventHubsConsumerProperties consumerProperties = bindingProperties.getExtendedConsumerProperties(
                "consume-in-0");
            CheckpointStore checkpointStore = mock(CheckpointStore.class);
            TestDefaultEventHubsNamespaceProcessorFactory factory = spy(new TestDefaultEventHubsNamespaceProcessorFactory(
                checkpointStore, new NamespaceProperties(), (key) -> {
                consumerProperties.setEventHubName(key.getDestination());
                consumerProperties.setConsumerGroup(key.getGroup());
                return consumerProperties;
            }));
            TestEventHubsMessageListenerContainer container = spy(new TestEventHubsMessageListenerContainer(factory));
            EventHubsInboundChannelAdapter messageProducer = spy(new EventHubsInboundChannelAdapter(container));
            EventHubsMessageChannelTestBinder binder = new EventHubsMessageChannelTestBinder(null, new EventHubsChannelProvisioner(), null, messageProducer);
            binder.setBindingProperties(bindingProperties);
            binder.setNamespaceProperties(namespaceProperties.getIfAvailable());
            checkpointStores.ifAvailable(binder::setCheckpointStore);
            return binder;
        }
    }

    static class TestDefaultEventHubsNamespaceProcessorFactory implements EventHubsProcessorFactory, DisposableBean {
        private DefaultEventHubsNamespaceProcessorFactory delegate;

        /**
         * Construct a factory with the provided {@link CheckpointStore}, namespace level properties and processor {@link PropertiesSupplier}.
         * @param checkpointStore the checkpoint store.
         * @param namespaceProperties the namespace properties.
         * @param supplier the {@link PropertiesSupplier} to supply {@link ProcessorProperties} for each event hub.
         */
        TestDefaultEventHubsNamespaceProcessorFactory(CheckpointStore checkpointStore,
                                                         NamespaceProperties namespaceProperties,
                                                         PropertiesSupplier<ConsumerIdentifier,
                                                             ProcessorProperties> supplier) {
            Assert.notNull(checkpointStore, "CheckpointStore must be provided.");
            this.delegate = new DefaultEventHubsNamespaceProcessorFactory(checkpointStore, namespaceProperties, supplier);
        }

        @Override
        public EventProcessorClient createProcessor(@NonNull String eventHub,
                                                    @NonNull String consumerGroup,
                                                    @NonNull EventHubsMessageListener listener,
                                                    @NonNull EventHubsErrorHandler errorHandler) {
            return this.delegate.createProcessor(eventHub, consumerGroup, listener, errorHandler);
        }

        @Override
        public EventProcessorClient createProcessor(String eventHub, String consumerGroup, EventHubsContainerProperties containerProperties) {
            return createProcessor(eventHub, consumerGroup, containerProperties.getMessageListener(), containerProperties.getErrorHandler());
        }

        @Override
        public void destroy() {
            this.delegate.destroy();
        }

        @Override
        public void addListener(EventHubsProcessorFactory.Listener listener) {
            this.delegate.addListener(listener);
        }

        @Override
        public boolean removeListener(EventHubsProcessorFactory.Listener listener) {
            return this.delegate.removeListener(listener);
        }
    }

    static class TestEventHubsMessageListenerContainer extends EventHubsMessageListenerContainer {

        private EventHubsProcessorFactory processorFactory;

        /**
         * Create an instance using the supplied processor factory.
         *
         * @param processorFactory the processor factory.
         */
        TestEventHubsMessageListenerContainer(EventHubsProcessorFactory processorFactory) {
            super(processorFactory, null);
            this.processorFactory = processorFactory;
        }

        public EventHubsProcessorFactory getProcessorFactory() {
            return processorFactory;
        }
    }

}
