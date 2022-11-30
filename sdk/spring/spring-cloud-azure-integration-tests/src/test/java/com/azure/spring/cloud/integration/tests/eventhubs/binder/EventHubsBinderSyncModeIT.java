// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.eventhubs.binder;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(value = { "eventhubs-binder", "sync" })
class EventHubsBinderSyncModeIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubsBinderSyncModeIT.class);
    private static final String MESSAGE = UUID.randomUUID().toString();

    @Autowired
    private Sinks.Many<Message<String>> many;

    private static final CountDownLatch LATCH = new CountDownLatch(1);

    @TestConfiguration
    static class TestConfig {

        @Bean
        Sinks.Many<Message<String>> many() {
            return Sinks.many().unicast().onBackpressureBuffer();
        }

        @Bean
        Supplier<Flux<Message<String>>> supply(Sinks.Many<Message<String>> many) {
            return () -> many.asFlux()
                             .doOnNext(m -> LOGGER.info("Manually sending message {}", m.getPayload()))
                             .doOnError(t -> LOGGER.error("Error encountered", t));
        }

        @Bean
        Consumer<Message<String>> consume() {
            return message -> {
                LOGGER.info("EventHubBinderSyncModeIT: New message received: '{}'", message.getPayload());
                if (message.getPayload().equals(EventHubsBinderSyncModeIT.MESSAGE) && message.getHeaders().containsKey("x-opt-enqueued-time")) {
                    LATCH.countDown();
                }
            };
        }

        @ServiceActivator(inputChannel = "errorChannel")
        public void processError(Message sendFailedMsg) {
            LOGGER.info("receive error message: '{}'", sendFailedMsg.getPayload());
        }
    }

    @Test
    void testSendAndReceiveMessage() throws InterruptedException {
        LOGGER.info("EventHubBinderSyncModeIT begin.");
        EventHubsBinderSyncModeIT.LATCH.await(15, TimeUnit.SECONDS);
        LOGGER.info("Send a message:" + MESSAGE + ".");
        many.emitNext(new GenericMessage<>(MESSAGE), Sinks.EmitFailureHandler.FAIL_FAST);
        assertThat(EventHubsBinderSyncModeIT.LATCH.await(30, TimeUnit.SECONDS)).isTrue();
        LOGGER.info("EventHubBinderSyncModeIT end.");
    }
}
