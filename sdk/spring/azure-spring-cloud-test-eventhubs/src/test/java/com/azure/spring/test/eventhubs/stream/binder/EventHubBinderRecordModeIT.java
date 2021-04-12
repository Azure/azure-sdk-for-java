// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.test.eventhubs.stream.binder;

import org.junit.jupiter.api.Assertions;
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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SpringBootTest(classes = EventHubBinderRecordModeIT.TestConfig.class)
@TestPropertySource(properties =
    {
        "spring.cloud.stream.eventhub.bindings.input.consumer.checkpoint-mode=RECORD",
        "spring.cloud.stream.bindings.consume-in-0.destination=test-eventhub-record",
        "spring.cloud.stream.bindings.supply-out-0.destination=test-eventhub-record",
        "spring.cloud.azure.eventhub.checkpoint-container=test-eventhub-record"
    })
public class EventHubBinderRecordModeIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubBinderManualModeIT.class);
    private static String message = UUID.randomUUID().toString();
    private static final AtomicInteger count = new AtomicInteger(0);

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
        public Consumer<Message<String>> consume() {
            return message -> {
                LOGGER.info("New message received: '{}'", message.getPayload());
                Assertions.assertEquals(message.getPayload(), EventHubBinderRecordModeIT.message);
                count.addAndGet(1);
            };
        }
    }

    @Test
    public void testSendAndReceiveMessage() throws InterruptedException {
        Thread.sleep(15000);
        many.emitNext(new GenericMessage<>(message), Sinks.EmitFailureHandler.FAIL_FAST);
        Thread.sleep(6000);
        Assertions.assertEquals(1, count.get());
    }
}
