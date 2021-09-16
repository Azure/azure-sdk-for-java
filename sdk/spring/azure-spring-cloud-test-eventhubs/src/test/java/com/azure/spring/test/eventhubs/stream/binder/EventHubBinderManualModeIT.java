// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.test.eventhubs.stream.binder;

import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.api.reactor.Checkpointer;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = EventHubBinderManualModeIT.TestConfig.class)
@TestPropertySource(properties =
    {
        "spring.cloud.stream.eventhub.bindings.input.consumer.checkpoint-mode=MANUAL",
        "spring.cloud.stream.bindings.consume-in-0.destination=test-eventhub-manual",
        "spring.cloud.stream.bindings.supply-out-0.destination=test-eventhub-manual",
        "spring.cloud.azure.eventhub.processor.checkpoint-store.container-name=test-eventhub-manual"
    })
public class EventHubBinderManualModeIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubBinderManualModeIT.class);
    private static String message = UUID.randomUUID().toString();
    private static CountDownLatch latch = new CountDownLatch(1);

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
                             .doOnNext(m -> LOGGER.info("Manually sending message {}", m))
                             .doOnError(t -> LOGGER.error("Error encountered", t));
        }

        @Bean
        public Consumer<Message<String>> consume() {
            return message -> {
                LOGGER.info("EventHubBinderManualModeIT: New message received: '{}'", message.getPayload());
                if (message.getPayload().equals(EventHubBinderManualModeIT.message)) {
                    Checkpointer checkpointer = (Checkpointer) message.getHeaders().get(AzureHeaders.CHECKPOINTER);
                    checkpointer.success().handle((r, ex) -> {
                        Assertions.assertNull(ex);
                    });
                    latch.countDown();
                }
            };
        }
    }

    @Test
    public void testSendAndReceiveMessage() throws InterruptedException {
        LOGGER.info("EventHubBinderManualModeIT begin.");
        EventHubBinderManualModeIT.latch.await(15, TimeUnit.SECONDS);
        LOGGER.info("Send a message:" + message + ".");
        many.emitNext(new GenericMessage<>(message), Sinks.EmitFailureHandler.FAIL_FAST);
        assertThat(EventHubBinderManualModeIT.latch.await(15, TimeUnit.SECONDS)).isTrue();
        LOGGER.info("EventHubBinderManualModeIT end.");
    }
}
