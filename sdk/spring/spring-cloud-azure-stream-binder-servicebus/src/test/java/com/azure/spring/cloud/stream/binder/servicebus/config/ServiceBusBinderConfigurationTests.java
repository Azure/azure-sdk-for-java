// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.resourcemanager.provisioning.ServiceBusProvisioner;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.cloud.stream.binder.servicebus.ServiceBusMessageChannelBinder;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusProducerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelResourceManagerProvisioner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.stream.binder.Binder;

import java.time.Duration;

import static com.azure.messaging.servicebus.models.SubQueue.DEAD_LETTER_QUEUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class ServiceBusBinderConfigurationTests {

    private static final String CONNECTION_STRING_FORMAT = "Endpoint=sb://%s.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ServiceBusBinderConfiguration.class));

    @Test
    void configurationNotMatchedWhenBinderBeanExist() {
        this.contextRunner
            .withBean(Binder.class, () -> mock(Binder.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean(ServiceBusBinderConfiguration.class);
                assertThat(context).doesNotHaveBean(ServiceBusMessageChannelBinder.class);
            });
    }

    @Test
    void shouldConfigureDefaultChannelProvisionerWhenNoResourceManagerProvided() {
        this.contextRunner
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusBinderConfiguration.class);
                assertThat(context).hasSingleBean(ServiceBusExtendedBindingProperties.class);
                assertThat(context).hasSingleBean(ServiceBusChannelProvisioner.class);
                assertThat(context).hasSingleBean(ServiceBusMessageChannelBinder.class);

                ServiceBusChannelProvisioner channelProvisioner = context.getBean(ServiceBusChannelProvisioner.class);
                assertThat(channelProvisioner).isNotInstanceOf(ServiceBusChannelResourceManagerProvisioner.class);
            });
    }

    @Test
    void shouldConfigureArmChannelProvisionerWhenResourceManagerProvided() {
        this.contextRunner
            .withBean(ServiceBusProvisioner.class, () -> mock(ServiceBusProvisioner.class))
            .withPropertyValues("spring.cloud.azure.servicebus.namespace=fake-namespace")
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusBinderConfiguration.class);
                assertThat(context).hasSingleBean(ServiceBusExtendedBindingProperties.class);
                assertThat(context).hasSingleBean(ServiceBusChannelProvisioner.class);
                assertThat(context).hasSingleBean(ServiceBusMessageChannelBinder.class);

                ServiceBusChannelProvisioner channelProvisioner = context.getBean(ServiceBusChannelProvisioner.class);
                assertThat(channelProvisioner).isInstanceOf(ServiceBusChannelResourceManagerProvisioner.class);
            });
    }

    @Test
    void testExtendedBindingPropertiesShouldBind() {
        String producerConnectionString = String.format(CONNECTION_STRING_FORMAT, "fake-producer-namespace");
        String consumerConnectionString = String.format(CONNECTION_STRING_FORMAT, "fake-consumer-namespace");

        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ServiceBusExtendedBindingPropertiesTestConfiguration.class))
            .withPropertyValues(
                "spring.cloud.stream.servicebus.bindings.input.consumer.domain-name=fake-consumer-domain",
                "spring.cloud.stream.servicebus.bindings.input.consumer.namespace=fake-consumer-namespace",
                "spring.cloud.stream.servicebus.bindings.input.consumer.connection-string=" + consumerConnectionString,
                "spring.cloud.stream.servicebus.bindings.input.consumer.entity-name=fake-consumer-sb-entity",
                "spring.cloud.stream.servicebus.bindings.input.consumer.entity-type=TOPIC",
                "spring.cloud.stream.servicebus.bindings.input.consumer.session-enabled=true",
                "spring.cloud.stream.servicebus.bindings.input.consumer.auto-complete=true",
                "spring.cloud.stream.servicebus.bindings.input.consumer.prefetch-count=1",
                "spring.cloud.stream.servicebus.bindings.input.consumer.sub-queue=DEAD_LETTER_QUEUE",
                "spring.cloud.stream.servicebus.bindings.input.consumer.receive-mode=RECEIVE_AND_DELETE",
                "spring.cloud.stream.servicebus.bindings.input.consumer.subscription-name=fake-consumer-subscription",
                "spring.cloud.stream.servicebus.bindings.input.consumer.max-auto-lock-renew-duration=2s",
                "spring.cloud.stream.servicebus.bindings.input.consumer.max-concurrent-calls=5",
                "spring.cloud.stream.servicebus.bindings.input.consumer.max-concurrent-sessions=6",
                "spring.cloud.stream.servicebus.bindings.input.consumer.requeue-rejected=true",

                "spring.cloud.stream.servicebus.bindings.input.producer.domain-name=fake-producer-domain",
                "spring.cloud.stream.servicebus.bindings.input.producer.namespace=fake-producer-namespace",
                "spring.cloud.stream.servicebus.bindings.input.producer.connection-string=" + producerConnectionString,
                "spring.cloud.stream.servicebus.bindings.input.producer.entity-name=fake-producer-sb-entity",
                "spring.cloud.stream.servicebus.bindings.input.producer.entity-type=QUEUE",
                "spring.cloud.stream.servicebus.bindings.input.producer.sync=true",
                "spring.cloud.stream.servicebus.bindings.input.producer.send-timeout=5m"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusExtendedBindingProperties.class);
                ServiceBusExtendedBindingProperties extendedBindingProperties =
                    context.getBean(ServiceBusExtendedBindingProperties.class);

                assertThat(extendedBindingProperties.getExtendedConsumerProperties("input")).isNotNull();

                ServiceBusConsumerProperties consumerProperties =
                    extendedBindingProperties.getExtendedConsumerProperties("input");
                assertEquals("fake-consumer-domain", consumerProperties.getDomainName());
                assertEquals("fake-consumer-namespace", consumerProperties.getNamespace());
                assertEquals(consumerConnectionString, consumerProperties.getConnectionString());
                assertEquals("fake-consumer-sb-entity", consumerProperties.getEntityName());
                assertEquals(ServiceBusEntityType.TOPIC, consumerProperties.getEntityType());
                assertTrue(consumerProperties.getSessionEnabled());
                assertTrue(consumerProperties.getAutoComplete());
                assertEquals(1, consumerProperties.getPrefetchCount());
                assertEquals(DEAD_LETTER_QUEUE, consumerProperties.getSubQueue());
                assertEquals(ServiceBusReceiveMode.RECEIVE_AND_DELETE, consumerProperties.getReceiveMode());
                assertEquals("fake-consumer-subscription", consumerProperties.getSubscriptionName());
                assertEquals(Duration.ofSeconds(2), consumerProperties.getMaxAutoLockRenewDuration());
                assertEquals(5, consumerProperties.getMaxConcurrentCalls());
                assertTrue(consumerProperties.isRequeueRejected());

                ServiceBusProducerProperties producerProperties =
                    extendedBindingProperties.getExtendedProducerProperties("input");
                assertEquals("fake-producer-domain", producerProperties.getDomainName());
                assertEquals("fake-producer-namespace", producerProperties.getNamespace());
                assertEquals(producerConnectionString, producerProperties.getConnectionString());
                assertEquals("fake-producer-sb-entity", producerProperties.getEntityName());
                assertEquals(ServiceBusEntityType.QUEUE, producerProperties.getEntityType());
                assertTrue(producerProperties.isSync());
                assertEquals(Duration.ofMinutes(5), producerProperties.getSendTimeout());
            });
    }

    @Test
    void clientFactoryCustomizerShouldBeConfigured() {
        this.contextRunner
            .withBean(ServiceBusProvisioner.class, () -> mock(ServiceBusProvisioner.class))
            .withPropertyValues("spring.cloud.azure.servicebus.namespace=fake-namespace")
            .run(context -> assertThat(context).hasSingleBean(ClientFactoryCustomizer.class));
    }

    @Test
    void builderCustomizerShouldBeConfiguredToClientFactoryCustomizer() {
        this.contextRunner
            .withBean(ServiceBusProvisioner.class, () -> mock(ServiceBusProvisioner.class))
            .withPropertyValues("spring.cloud.azure.servicebus.namespace=fake-namespace")
            .withBean("producer-customizer1", ServiceBusSenderClientBuilderCustomizer.class, ServiceBusSenderClientBuilderCustomizer::new)
            .withBean("processor-customizer1", ServiceBusProcessorClientBuilderCustomizer.class, ServiceBusProcessorClientBuilderCustomizer::new)
            .withBean("processor-customizer2", ServiceBusProcessorClientBuilderCustomizer.class, ServiceBusProcessorClientBuilderCustomizer::new)
            .withBean("session-processor-customizer1", ServiceBusSessionProcessorClientBuilderCustomizer.class, ServiceBusSessionProcessorClientBuilderCustomizer::new)
            .withBean("session-processor-customizer2", ServiceBusSessionProcessorClientBuilderCustomizer.class, ServiceBusSessionProcessorClientBuilderCustomizer::new)
            .withBean("session-processor-customizer3", ServiceBusSessionProcessorClientBuilderCustomizer.class, ServiceBusSessionProcessorClientBuilderCustomizer::new)
            .withBean("other-customizer1", OtherBuilderCustomizer.class, OtherBuilderCustomizer::new)
            .run(context -> {
                assertThat(context).hasSingleBean(ClientFactoryCustomizer.class);
                ClientFactoryCustomizer clientFactoryCustomizer = context.getBean(ClientFactoryCustomizer.class);

                ServiceBusBinderConfiguration.DefaultClientFactoryCustomizer defaultFactoryCustomizer = (ServiceBusBinderConfiguration.DefaultClientFactoryCustomizer) clientFactoryCustomizer;

                assertEquals(1, (int) defaultFactoryCustomizer.getSenderClientBuilderCustomizers().stream().count());
                assertEquals(2, (int) defaultFactoryCustomizer.getProcessorClientBuilderCustomizers().stream().count());
                assertEquals(3, (int) defaultFactoryCustomizer.getSessionProcessorClientBuilderCustomizers().stream().count());
            });
    }

    private static class ServiceBusSenderClientBuilderCustomizer implements AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSenderClientBuilder> {

        @Override
        public void customize(ServiceBusClientBuilder.ServiceBusSenderClientBuilder builder) {

        }
    }

    private static class ServiceBusProcessorClientBuilderCustomizer implements AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder> {

        @Override
        public void customize(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder) {

        }
    }

    private static class ServiceBusSessionProcessorClientBuilderCustomizer implements AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder> {

        @Override
        public void customize(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder builder) {

        }
    }


    private static class OtherBuilderCustomizer implements AzureServiceClientBuilderCustomizer<Object> {

        @Override
        public void customize(Object builder) {

        }
    }

}
