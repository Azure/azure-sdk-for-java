// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.test.eventhubs.stream.binder;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = EventHubBinderConsumingBatchModeIT.TestConfig.class)
@TestPropertySource(properties =
    {
        "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.checkpoint-mode=BATCH",
        "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.max-batch-size=10",
        "spring.cloud.stream.eventhub.bindings.consume-in-0.consumer.max-wait-time=2s",
        "spring.cloud.stream.bindings.consume-in-0.destination=test-eventhub-consuming-batch",
        "spring.cloud.stream.bindings.supply-out-0.destination=test-eventhub-consuming-batch",
        "spring.cloud.azure.eventhub.checkpoint-container=test-eventhub-consuming-batch",
        "spring.cloud.stream.bindings.consume-in-0.consumer.batch-mode=true"
    })
public class EventHubBinderConsumingBatchModeIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubBinderConsumingBatchModeIT.class);

    private static final String MESSAGE = UUID.randomUUID().toString();

    private static final CountDownLatch LATCH = new CountDownLatch(1);

    @Autowired
    private Sinks.Many<Message<String>> many;

    @EnableAutoConfiguration
    public static class TestConfig {

        @Bean
        public Sinks.Many<Message<String>> many() {
            return Sinks.many().unicast().onBackpressureBuffer();
        }

        @Bean
        public Supplier<Flux<Message<String>>> supply(Sinks.Many<Message<String>> many) {
            return () -> many.asFlux()
                .doOnNext(m -> LOGGER.info("Manually sending message {}", m.getPayload()))
                .doOnError(t -> LOGGER.error("Error encountered", t));
        }

        @Bean
        public Consumer<Message<List<String>>> consume() {
            return message -> {
                List<String> payload = message.getPayload();
                LOGGER.info("EventHubBinderBatchModeIT: New message received: '{}'", payload);
                if (payload.contains(EventHubBinderConsumingBatchModeIT.MESSAGE)) {
                    LATCH.countDown();
                }
            };
        }
    }

    @Test
    public void testSendAndReceiveMessage() throws InterruptedException {
        LOGGER.info("EventHubBinderBatchModeIT begin.");
        EventHubBinderConsumingBatchModeIT.LATCH.await(15, TimeUnit.SECONDS);
        LOGGER.info("Send a message:" + MESSAGE + ".");
        many.emitNext(new GenericMessage<>("\"" + MESSAGE + "\""), Sinks.EmitFailureHandler.FAIL_FAST);
        assertThat(EventHubBinderConsumingBatchModeIT.LATCH.await(600, TimeUnit.SECONDS)).isTrue();
        LOGGER.info("EventHubBinderBatchModeIT end.");
    }
}
