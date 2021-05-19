// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.sample.servicebus.binder;

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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = { ServiceBusQueueAndTopicBinderIT.TestQueueConfig.class,
    ServiceBusQueueAndTopicBinderIT.TestTopicConfig.class })
@TestPropertySource(locations = "classpath:application.yml")
public class ServiceBusQueueAndTopicBinderIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusQueueAndTopicBinderIT.class);

    private static String message = UUID.randomUUID().toString();

    private static final AtomicInteger count = new AtomicInteger(0);

    @Autowired
    private Sinks.Many<Message<String>> manyQueue;

    @Autowired
    private Sinks.Many<Message<String>> manyTopic;

    @EnableAutoConfiguration
    public static class TestQueueConfig {

        public static CountDownLatch latch = new CountDownLatch(2);

        @Bean
        public Sinks.Many<Message<String>> manyQueue() {
            return Sinks.many().unicast().onBackpressureBuffer();
        }

        @Bean
        public Supplier<Flux<Message<String>>> queueSupply(Sinks.Many<Message<String>> manyQueue) {
            return () -> manyQueue.asFlux()
                                  .doOnNext(m -> LOGGER.info("Manually sending message {}", m))
                                  .doOnError(t -> LOGGER.error("Error encountered", t));
        }

        @Bean
        public Consumer<Message<String>> queueConsume() {
            return message -> {
                LOGGER.info("New message received: '{}'", message);
                Assertions.assertEquals(message.getPayload(), ServiceBusQueueAndTopicBinderIT.message);
                count.addAndGet(1);
                latch.countDown();
            };
        }
    }

    @EnableAutoConfiguration
    public static class TestTopicConfig {

        public static CountDownLatch latch = new CountDownLatch(1);

        @Bean
        public Sinks.Many<Message<String>> manyTopic() {
            return Sinks.many().unicast().onBackpressureBuffer();
        }

        @Bean
        public Supplier<Flux<Message<String>>> topicSupply(Sinks.Many<Message<String>> manyTopic) {
            return () -> manyTopic.asFlux()
                                  .doOnNext(m -> LOGGER.info("Manually sending message {}", m))
                                  .doOnError(t -> LOGGER.error("Error encountered", t));
        }

        @Bean
        public Consumer<Message<String>> topicConsume() {
            return message -> {
                try {
                    TestQueueConfig.latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                LOGGER.info("New message received: '{}'", message);
                Assertions.assertEquals(message.getPayload(), ServiceBusQueueAndTopicBinderIT.message);
                count.addAndGet(1);
                this.latch.countDown();
            };
        }
    }

    @Test
    public void testSendAndReceiveMessage() throws InterruptedException {
        GenericMessage<String> genericMessage = new GenericMessage<>(message);
        manyQueue.emitNext(genericMessage, Sinks.EmitFailureHandler.FAIL_FAST);
        manyTopic.emitNext(genericMessage, Sinks.EmitFailureHandler.FAIL_FAST);
        TestQueueConfig.latch.await(3, TimeUnit.SECONDS);
        Assertions.assertEquals(1, count.get());
        TestQueueConfig.latch.countDown();
        assertThat(TestTopicConfig.latch.await(5, TimeUnit.SECONDS)).isTrue();
        Assertions.assertEquals(2, count.get());
    }

}
