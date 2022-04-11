// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static com.azure.messaging.servicebus.models.SubQueue.DEAD_LETTER_QUEUE;
import static com.azure.messaging.servicebus.models.SubQueue.TRANSFER_DEAD_LETTER_QUEUE;
import static com.azure.spring.cloud.autoconfigure.servicebus.ServiceBusTestUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
class AzureServiceBusAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusAutoConfiguration.class));

    @Test
    void configureWithoutCosmosClientBuilder() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ServiceBusClientBuilder.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProperties.class));
    }

    @Test
    void configureWithCosmosDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.servicebus.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProperties.class));
    }

    @Test
    void configureAzureServiceBusPropertiesWithGlobalDefaults() {
        AzureGlobalProperties azureProperties = new AzureGlobalProperties();
        azureProperties.getCredential().setClientId("azure-client-id");
        azureProperties.getCredential().setClientSecret("azure-client-secret");
        azureProperties.getRetry().getExponential().setBaseDelay(Duration.ofSeconds(2));

        this.contextRunner
            .withBean("azureProperties", AzureGlobalProperties.class, () -> azureProperties)
            .withPropertyValues(
                "spring.cloud.azure.servicebus.credential.client-id=servicebus-client-id",
                "spring.cloud.azure.servicebus.retry.exponential.base-delay=2m")
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProperties.class);
                final AzureServiceBusProperties properties = context.getBean(AzureServiceBusProperties.class);

                assertThat(properties.getCredential().getClientId()).isEqualTo("servicebus-client-id");
                assertThat(properties.getCredential().getClientSecret()).isEqualTo("azure-client-secret");
                assertThat(properties.getRetry().getExponential().getBaseDelay()).isEqualTo(Duration.ofMinutes(2));
            });
    }

    @Test
    void configureAmqpTransportTypeShouldApply() {
        this.contextRunner
            .withBean("azureProperties", AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withPropertyValues("spring.cloud.azure.servicebus.client.transport-type=AmqpWebSockets")
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProperties.class);
                final AzureServiceBusProperties properties = context.getBean(AzureServiceBusProperties.class);
                assertThat(properties.getClient().getTransportType()).isEqualTo(AmqpTransportType.AMQP_WEB_SOCKETS);
            });
    }

    @Test
    void configureRetryShouldApply() {
        this.contextRunner
            .withBean("azureProperties", AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withPropertyValues(
                "spring.cloud.azure.servicebus.retry.mode=fixed",
                "spring.cloud.azure.servicebus.retry.exponential.max-retries=5",
                "spring.cloud.azure.servicebus.retry.exponential.base-delay=10s",
                "spring.cloud.azure.servicebus.retry.exponential.max-delay=20s",
                "spring.cloud.azure.servicebus.retry.fixed.max-retries=6",
                "spring.cloud.azure.servicebus.retry.fixed.delay=30s",
                "spring.cloud.azure.servicebus.retry.try-timeout=40s"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProperties.class);
                final AzureServiceBusProperties properties = context.getBean(AzureServiceBusProperties.class);

                assertEquals(RetryOptionsProvider.RetryMode.FIXED, properties.getRetry().getMode());
                assertEquals(5, properties.getRetry().getExponential().getMaxRetries());
                assertEquals(Duration.ofSeconds(10), properties.getRetry().getExponential().getBaseDelay());
                assertEquals(Duration.ofSeconds(20), properties.getRetry().getExponential().getMaxDelay());
                assertEquals(6, properties.getRetry().getFixed().getMaxRetries());
                assertEquals(Duration.ofSeconds(30), properties.getRetry().getFixed().getDelay());
                assertEquals(Duration.ofSeconds(40), properties.getRetry().getTryTimeout());
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
                "spring.cloud.azure.servicebus.credential.client-id=servicebus-client-id",

                "spring.cloud.azure.servicebus.cross-entity-transactions=true",
                "spring.cloud.azure.servicebus.domain-name=fake-domain",
                "spring.cloud.azure.servicebus.namespace=fake-namespace",
                "spring.cloud.azure.servicebus.connection-string=" + connectionString,
                "spring.cloud.azure.servicebus.entity-name=fake-sb-entity",
                "spring.cloud.azure.servicebus.entity-type=TOPIC",

                "spring.cloud.azure.servicebus.producer.domain-name=fake-producer-domain",
                "spring.cloud.azure.servicebus.producer.namespace=fake-producer-namespace",
                "spring.cloud.azure.servicebus.producer.connection-string=" + producerConnectionString,
                "spring.cloud.azure.servicebus.producer.entity-name=fake-producer-sb-entity",
                "spring.cloud.azure.servicebus.producer.entity-type=QUEUE",

                "spring.cloud.azure.servicebus.consumer.domain-name=fake-consumer-domain",
                "spring.cloud.azure.servicebus.consumer.namespace=fake-consumer-namespace",
                "spring.cloud.azure.servicebus.consumer.connection-string=" + consumerConnectionString,
                "spring.cloud.azure.servicebus.consumer.entity-name=fake-consumer-sb-entity",
                "spring.cloud.azure.servicebus.consumer.entity-type=TOPIC",
                "spring.cloud.azure.servicebus.consumer.session-enabled=true",
                "spring.cloud.azure.servicebus.consumer.auto-complete=true",
                "spring.cloud.azure.servicebus.consumer.prefetch-count=1",
                "spring.cloud.azure.servicebus.consumer.sub-queue=DEAD_LETTER_QUEUE",
                "spring.cloud.azure.servicebus.consumer.receive-mode=RECEIVE_AND_DELETE",
                "spring.cloud.azure.servicebus.consumer.subscription-name=fake-consumer-subscription",
                "spring.cloud.azure.servicebus.consumer.max-auto-lock-renew-duration=2s",

                "spring.cloud.azure.servicebus.processor.domain-name=fake-processor-domain",
                "spring.cloud.azure.servicebus.processor.namespace=fake-processor-namespace",
                "spring.cloud.azure.servicebus.processor.connection-string=" + processorConnectionString,
                "spring.cloud.azure.servicebus.processor.entity-name=fake-processor-sb-entity",
                "spring.cloud.azure.servicebus.processor.entity-type=TOPIC",
                "spring.cloud.azure.servicebus.processor.session-enabled=true",
                "spring.cloud.azure.servicebus.processor.auto-complete=true",
                "spring.cloud.azure.servicebus.processor.prefetch-count=3",
                "spring.cloud.azure.servicebus.processor.sub-queue=TRANSFER_DEAD_LETTER_QUEUE",
                "spring.cloud.azure.servicebus.processor.receive-mode=PEEK_LOCK",
                "spring.cloud.azure.servicebus.processor.subscription-name=fake-processor-subscription",
                "spring.cloud.azure.servicebus.processor.max-auto-lock-renew-duration=4s",
                "spring.cloud.azure.servicebus.processor.max-concurrent-calls=5",
                "spring.cloud.azure.servicebus.processor.max-concurrent-sessions=6"

            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProperties.class);
                AzureServiceBusProperties properties = context.getBean(AzureServiceBusProperties.class);

                assertEquals("servicebus-client-id", properties.getCredential().getClientId());

                assertTrue(properties.getCrossEntityTransactions());
                assertEquals("fake-domain", properties.getDomainName());
                assertEquals("fake-namespace", properties.getNamespace());
                assertEquals(connectionString, properties.getConnectionString());
                assertEquals("fake-sb-entity", properties.getEntityName());
                assertEquals(ServiceBusEntityType.TOPIC, properties.getEntityType());

                AzureServiceBusProperties.Producer producer = properties.getProducer();
                assertEquals("fake-producer-domain", producer.getDomainName());
                assertEquals("fake-producer-namespace", producer.getNamespace());
                assertEquals(producerConnectionString, producer.getConnectionString());
                assertEquals("fake-producer-sb-entity", producer.getEntityName());
                assertEquals(ServiceBusEntityType.QUEUE, producer.getEntityType());

                AzureServiceBusProperties.Consumer consumer = properties.getConsumer();
                assertEquals("fake-consumer-domain", consumer.getDomainName());
                assertEquals("fake-consumer-namespace", consumer.getNamespace());
                assertEquals(consumerConnectionString, consumer.getConnectionString());
                assertEquals("fake-consumer-sb-entity", consumer.getEntityName());
                assertEquals(ServiceBusEntityType.TOPIC, consumer.getEntityType());
                assertTrue(consumer.getSessionEnabled());
                assertTrue(consumer.getAutoComplete());
                assertEquals(1, consumer.getPrefetchCount());
                assertEquals(DEAD_LETTER_QUEUE, consumer.getSubQueue());
                assertEquals(ServiceBusReceiveMode.RECEIVE_AND_DELETE, consumer.getReceiveMode());
                assertEquals("fake-consumer-subscription", consumer.getSubscriptionName());
                assertEquals(Duration.ofSeconds(2), consumer.getMaxAutoLockRenewDuration());

                AzureServiceBusProperties.Processor processor = properties.getProcessor();
                assertEquals("fake-processor-domain", processor.getDomainName());
                assertEquals("fake-processor-namespace", processor.getNamespace());
                assertEquals(processorConnectionString, processor.getConnectionString());
                assertEquals("fake-processor-sb-entity", processor.getEntityName());
                assertEquals(ServiceBusEntityType.TOPIC, processor.getEntityType());
                assertTrue(processor.getSessionEnabled());
                assertTrue(processor.getAutoComplete());
                assertEquals(3, processor.getPrefetchCount());
                assertEquals(TRANSFER_DEAD_LETTER_QUEUE, processor.getSubQueue());
                assertEquals(ServiceBusReceiveMode.PEEK_LOCK, processor.getReceiveMode());
                assertEquals("fake-processor-subscription", processor.getSubscriptionName());
                assertEquals(Duration.ofSeconds(4), processor.getMaxAutoLockRenewDuration());
                assertEquals(5, processor.getMaxConcurrentCalls());
                assertEquals(6, processor.getMaxConcurrentSessions());
            });
    }


}
