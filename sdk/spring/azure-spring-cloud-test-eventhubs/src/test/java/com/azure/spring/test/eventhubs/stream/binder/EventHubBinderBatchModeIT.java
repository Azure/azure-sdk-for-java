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

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = EventHubBinderBatchModeIT.TestConfig.class)
@TestPropertySource(properties = "spring.cloud.stream.eventhub.bindings.input.consumer.checkpoint-mode=BATCH")
public class EventHubBinderBatchModeIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubBinderBatchModeIT.class);

    private static String message = UUID.randomUUID().toString();

    @EnableAutoConfiguration
    public static class TestConfig {

        @Bean
        public Sinks.Many<Message<String>> many() {
            return Sinks.many().unicast().onBackpressureBuffer();
        }

        @Bean
        public Supplier<Flux<Message<String>>> supply(Sinks.Many<Message<String>> many) {
            return () -> many.asFlux()
                             .doOnNext(m -> LOGGER.info("Manually sending message {}", m))
                             .doOnError(t -> LOGGER.error("Error encountered", t));
        }

        @Bean
        public Consumer<Message<String>> consume() {
            return message -> {
                LOGGER.info("New message received: '{}'", message.getPayload());
                assertThat(message.getPayload().equals(EventHubBinderBatchModeIT.message)).isTrue();
            };
        }
    }

    @Autowired
    private Sinks.Many<Message<String>> many;

    @Test
    public void testSendAndReceiveMessage() {
        many.emitNext(new GenericMessage<>(message), Sinks.EmitFailureHandler.FAIL_FAST);
    }
}
