// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.test.eventhubs;

import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.api.reactor.Checkpointer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.concurrent.Exchanger;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SpringBootTest(classes = EventHubBinderConsumeErrorIT.TestConfig.class)
@TestPropertySource(properties = {
        "spring.cloud.stream.bindings.consume-in-0.destination=test-eventhub-message",
        "spring.cloud.stream.bindings.supply-out-0.destination=test-eventhub-message",
        "spring.cloud.azure.eventhub.processor.checkpoint-store.container-name=test-eventhub-message"
    })
public class EventHubBinderConsumeErrorIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubBinderConsumeErrorIT.class);
    private static final String MESSAGE = "Azure Spring Cloud EventHub Test";

    @Autowired
    private Sinks.One<Message<String>> one;

    @EnableAutoConfiguration
    public static class TestConfig {

        public static final Exchanger<String> EXCHANGER = new Exchanger<>();

        @Bean
        public Sinks.One<Message<String>> one() {
            return Sinks.one();
        }

        @Bean
        public Supplier<Mono<Message<String>>> supply(Sinks.One<Message<String>> one) {
            return () -> one.asMono()
                            .doOnNext(m -> LOGGER.info("Manually sending message {}", m))
                            .doOnError(t -> LOGGER.error("Error encountered", t));
        }

        @Bean
        public Consumer<Message<String>> consume() {
            return message -> {
                LOGGER.info("New message received: '{}'", message.getPayload());
                Checkpointer checkpointer = (Checkpointer) message.getHeaders().get(AzureHeaders.CHECKPOINTER);
                checkpointer.success();
                try {
                    String exchange = EXCHANGER.exchange(message.getPayload());
                    LOGGER.info("Consume exchange: {}", exchange);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                throw new RuntimeException();
            };
        }

        @ServiceActivator(inputChannel = "test-eventhub-message.$Default.errors")
        public void consumeError(Message<?> message) throws InterruptedException {
            EXCHANGER.exchange("ERROR!");
        }
    }

    @Test
    @Timeout(70)
    void integrationTest() throws InterruptedException {
        // Wait for eventhub initialization to complete
        Thread.sleep(15000);
        one.emitValue(new GenericMessage<>(MESSAGE), Sinks.EmitFailureHandler.FAIL_FAST);
        String msg = TestConfig.EXCHANGER.exchange(MESSAGE);
        Assertions.assertEquals(MESSAGE, msg);
        msg = TestConfig.EXCHANGER.exchange("");
        Assertions.assertEquals("ERROR!", msg);
    }
}
