// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.eventhubs.binder;

import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.checkpoint.Checkpointer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.concurrent.Exchanger;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(value = { "eventhubs-binder", "message" })
class EventHubsBinderConsumeErrorIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubsBinderConsumeErrorIT.class);
    private static final String MESSAGE = "Azure Spring Cloud EventHub Test";

    @Autowired
    private Sinks.One<Message<String>> one;

    @TestConfiguration
    static class TestConfig {

        static final Exchanger<String> EXCHANGER = new Exchanger<>();

        @Bean
        Sinks.One<Message<String>> one() {
            return Sinks.one();
        }

        @Bean
        Supplier<Mono<Message<String>>> supply(Sinks.One<Message<String>> one) {
            return () -> one.asMono()
                            .doOnNext(m -> LOGGER.info("Manually sending message {}", m))
                            .doOnError(t -> LOGGER.error("Error encountered", t));
        }

        @Bean
        Consumer<Message<String>> consume() {
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
                throw new RuntimeException("Consumption exception test");
            };
        }

        @Bean
        Consumer<ErrorMessage> consumeError() {
            return exception -> {
                try {
                    EXCHANGER.exchange("ERROR!");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

    @Test
    @Timeout(70)
    void integrationTest() throws InterruptedException {
        // Wait for eventhub initialization to complete
        Thread.sleep(20000);
        one.emitValue(new GenericMessage<>(MESSAGE), Sinks.EmitFailureHandler.FAIL_FAST);
        String msg = TestConfig.EXCHANGER.exchange(MESSAGE);
        Assertions.assertEquals(MESSAGE, msg);
        msg = TestConfig.EXCHANGER.exchange("");
        Assertions.assertEquals("ERROR!", msg);
    }
}
