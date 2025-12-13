// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.implementation.config;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.resourcemanager.implementation.provisioning.EventHubsProvisioner;
import com.azure.spring.cloud.stream.binder.eventhubs.config.EventHubsProcessorFactoryCustomizer;
import com.azure.spring.cloud.stream.binder.eventhubs.config.EventHubsProducerFactoryCustomizer;
import com.azure.spring.cloud.stream.binder.eventhubs.implementation.EventHubsMessageChannelBinder;
import com.azure.spring.cloud.stream.binder.eventhubs.implementation.EventHubsMessageChannelTestBinder;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsConsumerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsProducerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.implementation.provisioning.EventHubsChannelProvisioner;
import com.azure.spring.cloud.stream.binder.eventhubs.implementation.provisioning.EventHubsChannelResourceManagerProvisioner;
import com.azure.spring.integration.eventhubs.inbound.EventHubsInboundChannelAdapter;
import com.azure.spring.messaging.eventhubs.core.DefaultEventHubsNamespaceProducerFactory;
import com.azure.spring.messaging.eventhubs.core.EventHubsTemplate;
import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointMode;
import com.azure.spring.messaging.eventhubs.core.listener.EventHubsMessageListenerContainer;
import com.azure.spring.messaging.eventhubs.core.properties.EventHubsContainerProperties;
import com.azure.spring.messaging.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.messaging.eventhubs.core.DefaultEventHubsNamespaceProcessorFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
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
        properties.setNamespace("fake-namespace");
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
    void testExtendedBindingPropertiesShouldBind() {
        String producerConnectionString = String.format(CONNECTION_STRING_FORMAT, "fake-producer-namespace");
        String consumerConnectionString = String.format(CONNECTION_STRING_FORMAT, "fake-consumer-namespace");

        this.contextRunner
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
                assertThat(context).hasSingleBean(EventHubsMessageChannelBinder.class);
                EventHubsMessageChannelBinder binder =
                    context.getBean(EventHubsMessageChannelBinder.class);

                EventHubsConsumerProperties consumerProperties =
                    binder.getExtendedConsumerProperties("input");
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
                    binder.getExtendedProducerProperties("input");
                assertEquals("fake-producer-domain", producerProperties.getDomainName());
                assertEquals("fake-producer-namespace", producerProperties.getNamespace());
                assertEquals(producerConnectionString, producerProperties.getConnectionString());
                assertEquals("http://fake-producer-custom-endpoint.com", producerProperties.getCustomEndpointAddress());
                assertTrue(producerProperties.isSync());
                assertEquals(Duration.ofMinutes(5), producerProperties.getSendTimeout());
            });
    }

    @Test
    void producerFactoryCustomizerShouldBeConfigured() {
        AzureEventHubsProperties properties = new AzureEventHubsProperties();
        properties.setNamespace("fake-namespace");
        this.contextRunner
            .withBean(EventHubsProvisioner.class, () -> mock(EventHubsProvisioner.class))
            .withBean(AzureEventHubsProperties.class, () -> properties)
            .run(context -> assertThat(context).hasSingleBean(EventHubsProducerFactoryCustomizer.class));
    }

    @Test
    void processorFactoryCustomizerShouldBeConfigured() {
        AzureEventHubsProperties properties = new AzureEventHubsProperties();
        properties.setNamespace("fake-namespace");
        this.contextRunner
            .withBean(EventHubsProvisioner.class, () -> mock(EventHubsProvisioner.class))
            .withBean(AzureEventHubsProperties.class, () -> properties)
            .run(context -> assertThat(context).hasSingleBean(EventHubsProcessorFactoryCustomizer.class));
    }

    @Test
    void producerBuilderCustomizerShouldBeConfiguredToProducerFactoryCustomizer() {
        AzureEventHubsProperties properties = new AzureEventHubsProperties();
        properties.setNamespace("fake-namespace");
        this.contextRunner
            .withBean(EventHubsProvisioner.class, () -> mock(EventHubsProvisioner.class))
            .withBean(AzureEventHubsProperties.class, () -> properties)
            .withBean("producer-customizer1", EventHubBuilderCustomizer.class, EventHubBuilderCustomizer::new)
            .withBean("processor-customizer1", EventProcessorBuilderCustomizer.class, EventProcessorBuilderCustomizer::new)
            .withBean("processor-customizer2", EventProcessorBuilderCustomizer.class, EventProcessorBuilderCustomizer::new)
            .withBean("other-customizer1", OtherBuilderCustomizer.class, OtherBuilderCustomizer::new)
            .withBean("other-customizer2", OtherBuilderCustomizer.class, OtherBuilderCustomizer::new)
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubsProducerFactoryCustomizer.class);
                EventHubsProducerFactoryCustomizer producerClientFactoryCustomizer = context.getBean(EventHubsProducerFactoryCustomizer.class);
                EventHubsBinderConfiguration.DefaultProducerFactoryCustomizer defaultProducerFactoryCustomizer = (EventHubsBinderConfiguration.DefaultProducerFactoryCustomizer) producerClientFactoryCustomizer;

                assertEquals(1, (int) defaultProducerFactoryCustomizer.getClientBuilderCustomizers().stream().count());
            });
    }

    @Test
    void processorBuilderCustomizerShouldBeConfiguredToProcessorFactoryCustomizer() {
        AzureEventHubsProperties properties = new AzureEventHubsProperties();
        properties.setNamespace("fake-namespace");
        this.contextRunner
            .withBean(EventHubsProvisioner.class, () -> mock(EventHubsProvisioner.class))
            .withBean(AzureEventHubsProperties.class, () -> properties)
            .withBean("producer-customizer1", EventHubBuilderCustomizer.class, EventHubBuilderCustomizer::new)
            .withBean("processor-customizer1", EventProcessorBuilderCustomizer.class, EventProcessorBuilderCustomizer::new)
            .withBean("processor-customizer2", EventProcessorBuilderCustomizer.class, EventProcessorBuilderCustomizer::new)
            .withBean("other-customizer1", OtherBuilderCustomizer.class, OtherBuilderCustomizer::new)
            .withBean("other-customizer2", OtherBuilderCustomizer.class, OtherBuilderCustomizer::new)
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubsProcessorFactoryCustomizer.class);
                EventHubsProcessorFactoryCustomizer processorClientFactoryCustomizer = context.getBean(EventHubsProcessorFactoryCustomizer.class);
                EventHubsBinderConfiguration.DefaultProcessorFactoryCustomizer defaultProcessorFactoryCustomizer = (EventHubsBinderConfiguration.DefaultProcessorFactoryCustomizer) processorClientFactoryCustomizer;

                assertEquals(2, (int) defaultProcessorFactoryCustomizer.getProcessorClientBuilderCustomizers().stream().count());
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
            DefaultEventHubsNamespaceProcessorFactory factory = spy(new DefaultEventHubsNamespaceProcessorFactory(
                checkpointStore, new NamespaceProperties(), (key) -> {
                consumerProperties.setEventHubName(key.getDestination());
                consumerProperties.setConsumerGroup(key.getGroup());
                return consumerProperties;
            }));
            EventHubsMessageListenerContainer container = spy(new EventHubsMessageListenerContainer(factory, new EventHubsContainerProperties()));
            EventHubsInboundChannelAdapter messageProducer = spy(new EventHubsInboundChannelAdapter(container));
            EventHubsMessageChannelTestBinder binder = new EventHubsMessageChannelTestBinder(null, new EventHubsChannelProvisioner(), null, messageProducer);
            binder.setBindingProperties(bindingProperties);
            binder.setNamespaceProperties(namespaceProperties.getIfAvailable());
            checkpointStores.ifAvailable(binder::setCheckpointStore);
            return binder;
        }
    }

    private static class EventHubBuilderCustomizer implements AzureServiceClientBuilderCustomizer<EventHubClientBuilder> {

        @Override
        public void customize(EventHubClientBuilder builder) {

        }
    }

    private static class EventProcessorBuilderCustomizer implements AzureServiceClientBuilderCustomizer<EventProcessorClientBuilder> {

        @Override
        public void customize(EventProcessorClientBuilder builder) {

        }
    }


    private static class OtherBuilderCustomizer implements AzureServiceClientBuilderCustomizer<Object> {

        @Override
        public void customize(Object builder) {

        }
    }

    @Test
    void testCustomTokenCredentialConfiguration() {
        String connectionString = String.format(CONNECTION_STRING_FORMAT, "test-namespace");

        this.contextRunner
            .withConfiguration(AutoConfigurations.of(CustomTokenCredentialConfiguration.class))
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + connectionString,
                "spring.cloud.azure.eventhubs.credential.token-credential-bean-name=customTokenCredential"
            )
            .run(context -> {
                // Verify that the custom token credential bean exists
                assertThat(context).hasBean("customTokenCredential");
                TokenCredential customCredential = context.getBean("customTokenCredential", TokenCredential.class);
                assertThat(customCredential).isNotNull();

                // Verify that the properties contain the correct credential bean name
                AzureEventHubsProperties eventHubsProperties = context.getBean(AzureEventHubsProperties.class);
                assertThat(eventHubsProperties).isNotNull();
                assertThat(eventHubsProperties.getCredential()).isNotNull();
                assertThat(eventHubsProperties.getCredential().getTokenCredentialBeanName())
                    .as("The token-credential-bean-name property should be set to customTokenCredential")
                    .isEqualTo("customTokenCredential");

                // Verify the EventHubsProducerFactoryCustomizer is configured and can apply credential settings
                assertThat(context).hasSingleBean(EventHubsProducerFactoryCustomizer.class);
                EventHubsProducerFactoryCustomizer producerFactoryCustomizer =
                    context.getBean(EventHubsProducerFactoryCustomizer.class);
                assertThat(producerFactoryCustomizer).isNotNull();

                // Verify it's the default customizer with token credential resolver
                assertThat(producerFactoryCustomizer)
                    .isInstanceOf(EventHubsBinderConfiguration.DefaultProducerFactoryCustomizer.class);
            });
    }

    @Test
    void testCustomTokenCredentialConfigurationWithBinder() {
        String connectionString = String.format(CONNECTION_STRING_FORMAT, "test-namespace");

        this.contextRunner
            .withConfiguration(AutoConfigurations.of(CustomTokenCredentialConfiguration.class))
            .withBean(CheckpointStore.class, () -> mock(CheckpointStore.class))
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + connectionString,
                "spring.cloud.azure.eventhubs.credential.token-credential-bean-name=customTokenCredential",
                "spring.cloud.azure.eventhubs.namespace=test-namespace"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubsMessageChannelBinder.class);
                EventHubsMessageChannelBinder binder = context.getBean(EventHubsMessageChannelBinder.class);

                TokenCredential customCredential = context.getBean("customTokenCredential", TokenCredential.class);
                AzureEventHubsProperties eventHubsProperties = context.getBean(AzureEventHubsProperties.class);

                // Test Producer Factory
                // Verify that credential resolver is properly configured in the producer factory created by binder
                EventHubsTemplate eventHubsTemplate = ReflectionTestUtils.invokeMethod(binder, "getEventHubTemplate");
                assertThat(eventHubsTemplate).isNotNull();

                DefaultEventHubsNamespaceProducerFactory producerFactory = (DefaultEventHubsNamespaceProducerFactory) ReflectionTestUtils.getField(eventHubsTemplate, "producerFactory");
                assertThat(producerFactory).isNotNull();

                // Use reflection to access the tokenCredentialResolver field in producer factory
                Field producerResolverField = producerFactory.getClass().getDeclaredField("tokenCredentialResolver");
                producerResolverField.setAccessible(true);
                Object producerResolver = producerResolverField.get(producerFactory);
                assertThat(producerResolver)
                    .as("TokenCredentialResolver should be configured in the binder's producer factory")
                    .isNotNull();

                // Verify that producer resolver can resolve the custom credential
                @SuppressWarnings("unchecked")
                AzureCredentialResolver<TokenCredential> typedProducerResolver =
                    (AzureCredentialResolver<TokenCredential>) producerResolver;
                TokenCredential producerResolvedCredential = typedProducerResolver.resolve(eventHubsProperties);
                assertThat(producerResolvedCredential)
                    .as("The resolved credential in binder's producer factory should be the customTokenCredential bean")
                    .isSameAs(customCredential);

                // Test Processor Factory
                // Get the ProcessorFactory through reflection (it's created lazily in getProcessorFactory)
                Object processorFactory = ReflectionTestUtils.invokeMethod(binder, "getProcessorFactory");
                assertThat(processorFactory).isNotNull();

                // Use reflection to access the tokenCredentialResolver field in processor factory
                Field processorResolverField = processorFactory.getClass().getDeclaredField("tokenCredentialResolver");
                processorResolverField.setAccessible(true);
                Object processorResolver = processorResolverField.get(processorFactory);
                assertThat(processorResolver)
                    .as("TokenCredentialResolver should be configured in the binder's processor factory")
                    .isNotNull();

                // Verify that processor resolver can resolve the custom credential
                @SuppressWarnings("unchecked")
                AzureCredentialResolver<TokenCredential> typedProcessorResolver =
                    (AzureCredentialResolver<TokenCredential>) processorResolver;
                TokenCredential processorResolvedCredential = typedProcessorResolver.resolve(eventHubsProperties);
                assertThat(processorResolvedCredential)
                    .as("The resolved credential in binder's processor factory should be the customTokenCredential bean")
                    .isSameAs(customCredential);
            });
    }

    @Configuration
    public static class CustomTokenCredentialConfiguration {
        @Bean
        public TokenCredential customTokenCredential() {
            return mock(TokenCredential.class);
        }
    }

}
