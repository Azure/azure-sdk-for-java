// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.sample.servicebus.binder;

import com.azure.spring.integration.core.api.Checkpointer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.azure.spring.integration.core.AzureHeaders.CHECKPOINTER;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = { ServiceBusQueueAndTopicBinderWithPropertiesIT.TestQueueConfig.class,
    ServiceBusQueueAndTopicBinderWithPropertiesIT.TestTopicConfig.class })
@ActiveProfiles("properties")
public class ServiceBusQueueAndTopicBinderWithPropertiesIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusQueueAndTopicBinderWithPropertiesIT.class);

    private static String message = UUID.randomUUID().toString();

    private static CountDownLatch latch = new CountDownLatch(2);
    private static boolean queueError = false;

    @Autowired
    private Sinks.Many<Message<String>> manyQueue;

    @Autowired
    private Sinks.Many<Message<String>> manyTopic;

    @EnableAutoConfiguration
    public static class TestQueueConfig {

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
                LOGGER.info("---Test queue new message received: '{}'", message);
                if (message.getPayload().equals(ServiceBusQueueAndTopicBinderWithPropertiesIT.message)) {
                    latch.countDown();
                }
                Checkpointer checkpointer = (Checkpointer) message.getHeaders().get(CHECKPOINTER);
                checkpointer.success().handle((r, ex) -> {
                    if (ex != null) {
                        queueError = true;
                    }
                    return null;
                });

            };
        }
    }

    @EnableAutoConfiguration
    public static class TestTopicConfig {

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
                LOGGER.info("---Test topic new message received: '{}'", message);
                if (message.getPayload().equals(ServiceBusQueueAndTopicBinderWithPropertiesIT.message)) {
                    latch.countDown();
                }
            };
        }
    }

    @Test
    public void testSingleServiceBusSendAndReceiveMessage() throws InterruptedException {
        LOGGER.info("SingleServiceBusQueueAndTopicBinderIT begin.");

        LOGGER.info("Send a message:" + message + " to the queue.");
        manyQueue.emitNext(MessageBuilder.withPayload(message).build(), Sinks.EmitFailureHandler.FAIL_FAST);
        LOGGER.info("Send a message:" + message + " to the topic.");
        manyTopic.emitNext(MessageBuilder.withPayload(message).build(), Sinks.EmitFailureHandler.FAIL_FAST);

        assertThat(ServiceBusQueueAndTopicBinderWithPropertiesIT.latch.await(15, TimeUnit.SECONDS)).isTrue();
        assertThat(queueError).isTrue();
        LOGGER.info("SingleServiceBusQueueAndTopicBinderIT end.");
    }

}
