// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.integration.tests.servicebus.binder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class TestServiceBusMultiBinders {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestServiceBusMultiBinders.class);

    private static String message = UUID.randomUUID().toString();

    static final Map<String, CountDownLatch> LATCH = new ConcurrentHashMap<>();

    @TestConfiguration
    static class TestQueueConfig {

        @Autowired
        private Environment environment;

        @Bean
        Sinks.Many<Message<String>> manyQueue() {
            return Sinks.many().unicast().onBackpressureBuffer();
        }

        @Bean
        Supplier<Flux<Message<String>>> queueSupply(Sinks.Many<Message<String>> manyQueue) {
            return () -> manyQueue.asFlux()
                                  .doOnNext(m -> LOGGER.info("Manually sending message {}", m))
                                  .doOnError(t -> LOGGER.error("Error encountered", t));
        }

        @Bean
        Consumer<Message<String>> queueConsume() {
            return message -> {
                LOGGER.info("Test queue new message received: '{}'", message);
                if (message.getPayload().equals(TestServiceBusMultiBinders.message)) {
                    LATCH.get(String.join("", environment.getActiveProfiles())).countDown();
                }
            };
        }
    }

    @TestConfiguration
    static class TestTopicConfig {

        @Autowired
        private Environment environment;

        @Bean
        Sinks.Many<Message<String>> manyTopic() {
            return Sinks.many().unicast().onBackpressureBuffer();
        }

        @Bean
        Supplier<Flux<Message<String>>> topicSupply(Sinks.Many<Message<String>> manyTopic) {
            return () -> manyTopic.asFlux()
                                  .doOnNext(m -> LOGGER.info("Manually sending message {}", m))
                                  .doOnError(t -> LOGGER.error("Error encountered", t));
        }

        @Bean
        Consumer<Message<String>> topicConsume() {
            return message -> {
                LOGGER.info("Test topic new message received: '{}'", message);
                if (message.getPayload().equals(TestServiceBusMultiBinders.message)) {
                    LATCH.get(String.join("", environment.getActiveProfiles())).countDown();
                }
            };
        }
    }

    protected void exchangeMessageAndVerify(String activeProfile,
                                            Sinks.Many<Message<String>> manyQueue,
                                            Sinks.Many<Message<String>> manyTopic) throws InterruptedException {
        GenericMessage<String> genericMessage = new GenericMessage<>(message);

        LOGGER.info("Send a message:" + message + " to the queue.");
        manyQueue.emitNext(genericMessage, Sinks.EmitFailureHandler.FAIL_FAST);
        LOGGER.info("Send a message:" + message + " to the topic.");
        manyTopic.emitNext(genericMessage, Sinks.EmitFailureHandler.FAIL_FAST);

        assertThat(LATCH.get(activeProfile).await(30, TimeUnit.SECONDS)).isTrue();
    }

}
