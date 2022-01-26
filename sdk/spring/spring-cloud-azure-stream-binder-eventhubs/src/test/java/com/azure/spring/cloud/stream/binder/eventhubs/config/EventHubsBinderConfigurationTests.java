// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.config;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.EventHubsMessageChannelBinder;
import com.azure.spring.cloud.stream.binder.eventhubs.TestEventHubsMessageChannelBinder;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsConsumerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubsChannelProvisioner;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubsChannelResourceManagerProvisioner;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.core.factory.AbstractAzureAmqpClientBuilderFactory;
import com.azure.spring.eventhubs.core.EventHubsProcessorContainer;
import com.azure.spring.eventhubs.core.processor.DefaultEventHubsNamespaceProcessorFactory;
import com.azure.spring.eventhubs.core.processor.EventHubsProcessorFactory;
import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.eventhubs.core.properties.ProcessorProperties;
import com.azure.spring.integration.eventhubs.inbound.EventHubsInboundChannelAdapter;
import com.azure.spring.messaging.ConsumerIdentifier;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.resourcemanager.provisioning.eventhubs.EventHubsProvisioner;
import com.azure.spring.service.eventhubs.processor.EventProcessingListener;
import com.azure.spring.service.implementation.eventhubs.factory.EventProcessorClientBuilderFactory;
import com.azure.spring.service.implementation.eventhubs.properties.EventProcessorClientProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.Assert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventHubsBinderConfigurationTests {

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

                TestEventHubsMessageChannelBinder channelBinder = context.getBean(TestEventHubsMessageChannelBinder.class);
                assertThat(channelBinder).isNotNull();
                ConsumerDestination destination = mock(ConsumerDestination.class);
                when(destination.getName()).thenReturn("dest");
                CheckpointStore checkpointStore = mock(CheckpointStore.class);
                channelBinder.setCheckpointStore(checkpointStore);
                EventHubsInboundChannelAdapter channelAdapter = (EventHubsInboundChannelAdapter) channelBinder.createConsumerEndpoint(
                    destination, "test", new ExtendedConsumerProperties<>(consumerProperties));
                TestEventHubsProcessorContainer processorContainer =
                    (TestEventHubsProcessorContainer) ReflectionTestUtils.getField(channelAdapter, "processorContainer");
                TestDefaultEventHubsNamespaceProcessorFactory processorFactory = (TestDefaultEventHubsNamespaceProcessorFactory) processorContainer.getProcessorFactory();
                TestEventProcessorClientBuilderFactory clientBuilderFactory =
                    (TestEventProcessorClientBuilderFactory) processorFactory.createEventProcessorClientBuilderFactory(null, consumerProperties);
                EventProcessorClientBuilder processorClientBuilder = clientBuilderFactory.build();
                processorClientBuilder.buildEventProcessorClient();
                verify(processorClientBuilder, times(1)).prefetchCount(150);
            });
    }

    @Configuration
    @EnableConfigurationProperties(EventHubsExtendedBindingProperties.class)
    static class TestProcessorContainerConfiguration {

        @Bean
        public TestEventHubsMessageChannelBinder eventHubBinder(EventHubsExtendedBindingProperties bindingProperties,
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
            TestEventHubsProcessorContainer container = spy(new TestEventHubsProcessorContainer(factory));
            EventHubsInboundChannelAdapter messageProducer = spy(new EventHubsInboundChannelAdapter(container,
                "dest", "group", consumerProperties.getCheckpoint()));
            TestEventHubsMessageChannelBinder binder = new TestEventHubsMessageChannelBinder(null,
                new EventHubsChannelProvisioner(), null, messageProducer);
            binder.setBindingProperties(bindingProperties);
            binder.setNamespaceProperties(namespaceProperties.getIfAvailable());
            checkpointStores.ifAvailable(binder::setCheckpointStore);
            return binder;
        }
    }

    static class TestDefaultEventHubsNamespaceProcessorFactory implements EventHubsProcessorFactory, DisposableBean {
        private DefaultEventHubsNamespaceProcessorFactory delegate;

        public AbstractAzureAmqpClientBuilderFactory<EventProcessorClientBuilder> createEventProcessorClientBuilderFactory(EventProcessingListener listener,
                                                                                                                           ProcessorProperties processorProperties) {
            TestEventProcessorClientBuilderFactory factory =
                new TestEventProcessorClientBuilderFactory(processorProperties);
            factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_INTEGRATION_EVENT_HUBS);
            return factory;
        }

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
        public EventProcessorClient createProcessor(@NonNull String eventHub, @NonNull String consumerGroup,
                                                    @NonNull EventProcessingListener listener) {
            return this.delegate.createProcessor(eventHub, consumerGroup, listener);
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

    static class TestEventHubsProcessorContainer extends EventHubsProcessorContainer {

        private EventHubsProcessorFactory processorFactory;

        /**
         * Create an instance using the supplied processor factory.
         *
         * @param processorFactory the processor factory.
         */
        TestEventHubsProcessorContainer(EventHubsProcessorFactory processorFactory) {
            super(processorFactory);
            this.processorFactory = processorFactory;
        }

        public EventHubsProcessorFactory getProcessorFactory() {
            return processorFactory;
        }
    }

    static class TestEventProcessorClientBuilderFactory extends EventProcessorClientBuilderFactory {

        TestEventProcessorClientBuilderFactory(EventProcessorClientProperties eventProcessorClientProperties) {
            super(eventProcessorClientProperties, null, mock(EventProcessingListener.class));
        }

        @Override
        public EventProcessorClientBuilder createBuilderInstance() {
            return mock(EventProcessorClientBuilder.class);
        }
    }
}
