// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhub.stream.binder;

import com.azure.spring.eventhub.stream.binder.properties.EventHubConsumerProperties;
import com.azure.spring.eventhub.stream.binder.properties.EventHubProducerProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.*;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;

import static com.azure.core.amqp.AmqpRetryMode.FIXED;
import static com.azure.core.amqp.AmqpTransportType.AMQP_WEB_SOCKETS;
import static com.azure.messaging.eventhubs.LoadBalancingStrategy.GREEDY;
import static com.azure.spring.integration.core.api.CheckpointMode.BATCH;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
    "spring.cloud.azure.eventhub.connection-string=eventhub-namespace-connection-string",
    "spring.cloud.stream.function.definition=consume;supply",
    "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.start-position=LATEST",
    "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.checkpoint-count=10",
    "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.share-connection=true",
    "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.prefetch-count=3",
    "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.custom-endpoint-address=custom-endpoint-address",
    "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.send-timeout=10000",
    "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.checkpoint-interval=5",
    "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.checkpoint-mode=BATCH",
    "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.load-balancing-strategy=GREEDY",
    "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.transport=AMQP_WEB_SOCKETS",
    "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.track-last-enqueued-event-properties=true",
    "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.partition-ownership-expiration-interval=30",
    "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.load-balancing-update-interval=10",
    "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.retry-options.delay=100",
    "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.retry-options.max-delay=200",
    "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.retry-options.max-retries=3",
    "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.retry-options.mode=FIXED",
    "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.retry-options.try-timeout=10000",
    "spring.cloud.stream.eventhub.bindings.supply-out-0.producer.send-timeout=1000",
    "spring.cloud.stream.eventhub.bindings.supply-out-0.producer.custom-endpoint-address=custom-endpoint-address",
    "spring.cloud.stream.eventhub.bindings.supply-out-0.producer.prefetch-count=4",
    "spring.cloud.stream.eventhub.bindings.supply-out-0.producer.retry-options.delay=100",
    "spring.cloud.stream.eventhub.bindings.supply-out-0.producer.retry-options.max-delay=200",
    "spring.cloud.stream.eventhub.bindings.supply-out-0.producer.retry-options.max-retries=3",
    "spring.cloud.stream.eventhub.bindings.supply-out-0.producer.retry-options.mode=FIXED",
    "spring.cloud.stream.eventhub.bindings.supply-out-0.producer.retry-options.try-timeout=10000",
    "spring.cloud.stream.eventhub.bindings.supply-out-0.producer.share-connection=true",
    "spring.cloud.stream.eventhub.bindings.supply-out-0.producer.sync=true",
    "spring.cloud.stream.eventhub.bindings.supply-out-0.producer.transport=AMQP_WEB_SOCKETS"})
@DirtiesContext
public class EventHubBinderExtendedPropertiesTest {

    @Autowired
    private ConfigurableApplicationContext context;

    interface CustomBindingForExtendedPropertyTesting {
        @Input("consume-in-0")
        SubscribableChannel consumeIn();

        @Output("supply-out-0")
        MessageChannel supplyOut();

    }

    @EnableBinding(CustomBindingForExtendedPropertyTesting.class)
    @EnableAutoConfiguration
    public static class KafkaMetricsTestConfig {

        @StreamListener("consume-in-0")
        @SendTo("supply-out-0")
        public String process(String payload) {
            return payload;
        }
    }

    @Test
    public void testEventHubBinderExtendedProperties() {
        BinderFactory binderFactory = context.getBeanFactory()
            .getBean(BinderFactory.class);
        Binder<MessageChannel, ? extends ConsumerProperties, ? extends ProducerProperties> eventhubBinder = binderFactory
            .getBinder("eventhub", MessageChannel.class);

        EventHubConsumerProperties eventHubConsumerProperties = (EventHubConsumerProperties) ((ExtendedPropertiesBinder) eventhubBinder)
            .getExtendedConsumerProperties("consume-in-0");

        assertThat(eventHubConsumerProperties.getCheckpointCount()).isEqualTo(10);
        assertThat(eventHubConsumerProperties.getCheckpointInterval()).isEqualTo(Duration.ofMillis(5));
        assertThat(eventHubConsumerProperties.getCustomEndpointAddress()).isEqualTo("custom-endpoint-address");
        assertThat(eventHubConsumerProperties.getPrefetchCount()).isEqualTo(3);
        assertThat(eventHubConsumerProperties.getPartitionOwnershipExpirationInterval()).isEqualTo(Duration.ofMillis(300));
        assertThat(eventHubConsumerProperties.getSendTimeout()).isEqualTo(10000);
        assertThat(eventHubConsumerProperties.getLoadBalancingUpdateInterval()).isEqualTo(Duration.ofMillis(10));
        assertThat(eventHubConsumerProperties.getCheckpointMode()).isEqualTo(BATCH);
        assertThat(eventHubConsumerProperties.getLoadBalancingStrategy()).isEqualTo(GREEDY);
        assertThat(eventHubConsumerProperties.getRetryOptions().getTryTimeout()).isEqualTo(Duration.ofSeconds(10));
        assertThat(eventHubConsumerProperties.getRetryOptions().getMaxRetries()).isEqualTo(3);
        assertThat(eventHubConsumerProperties.getRetryOptions().getDelay()).isEqualTo(Duration.ofMillis(100));
        assertThat(eventHubConsumerProperties.getRetryOptions().getMaxDelay()).isEqualTo(Duration.ofMillis(200));
        assertThat(eventHubConsumerProperties.getRetryOptions().getMode()).isEqualTo(FIXED);
        assertThat(eventHubConsumerProperties.getTransport()).isEqualTo(AMQP_WEB_SOCKETS);
        assertThat(eventHubConsumerProperties.isShareConnection()).isEqualTo(true);
        assertThat(eventHubConsumerProperties.isTrackLastEnqueuedEventProperties()).isEqualTo(true);

        EventHubProducerProperties eventHubProducerProperties = (EventHubProducerProperties) ((ExtendedPropertiesBinder) eventhubBinder)
            .getExtendedProducerProperties("supply-out-0");

        assertThat(eventHubProducerProperties.getPrefetchCount()).isEqualTo(4);
        assertThat(eventHubProducerProperties.getCustomEndpointAddress()).isEqualTo("custom-endpoint-address");
        assertThat(eventHubProducerProperties.getSendTimeout()).isEqualTo(1000);
        assertThat(eventHubProducerProperties.getTransport()).isEqualTo(AMQP_WEB_SOCKETS);
        assertThat(eventHubProducerProperties.getRetryOptions().getDelay()).isEqualTo(Duration.ofMillis(100));
        assertThat(eventHubProducerProperties.getRetryOptions().getMaxRetries()).isEqualTo(3);
        assertThat(eventHubProducerProperties.getRetryOptions().getTryTimeout()).isEqualTo(Duration.ofSeconds(10));
        assertThat(eventHubProducerProperties.getRetryOptions().getMaxDelay()).isEqualTo(Duration.ofMillis(200));
        assertThat(eventHubProducerProperties.getRetryOptions().getMode()).isEqualTo(FIXED);
        assertThat(eventHubProducerProperties.isShareConnection()).isEqualTo(true);
        assertThat(eventHubProducerProperties.isSync()).isEqualTo(true);

    }
}
