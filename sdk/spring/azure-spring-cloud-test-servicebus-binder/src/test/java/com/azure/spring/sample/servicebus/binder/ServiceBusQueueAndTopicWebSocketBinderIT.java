// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.sample.servicebus.binder;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = { ServiceBusQueueAndTopicWebSocketBinderIT.TestQueueConfig.class,
    ServiceBusQueueAndTopicWebSocketBinderIT.TestTopicConfig.class })
@ActiveProfiles("websocket")
public class ServiceBusQueueAndTopicWebSocketBinderIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusQueueAndTopicWebSocketBinderIT.class);

    private static String message = UUID.randomUUID().toString();

    private static CountDownLatch latch = new CountDownLatch(2);

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
                if (message.getPayload().equals(ServiceBusQueueAndTopicWebSocketBinderIT.message)) {
                    latch.countDown();
                }
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
                if (message.getPayload().equals(ServiceBusQueueAndTopicWebSocketBinderIT.message)) {
                    latch.countDown();
                }
            };
        }
    }

    @Test
    public void testServiceBusSendAndReceiveMessage() throws InterruptedException {
        LOGGER.info("SingleServiceBusQueueAndTopicBinderIT begin.");
        GenericMessage<String> genericMessage = new GenericMessage<>(message);

        LOGGER.info("Send a message:" + message + " to the queue.");
        manyQueue.emitNext(genericMessage, Sinks.EmitFailureHandler.FAIL_FAST);
        LOGGER.info("Send a message:" + message + " to the topic.");
        manyTopic.emitNext(genericMessage, Sinks.EmitFailureHandler.FAIL_FAST);

        assertThat(ServiceBusQueueAndTopicWebSocketBinderIT.latch.await(15, TimeUnit.SECONDS)).isTrue();
        LOGGER.info("SingleServiceBusQueueAndTopicBinderIT end.");
    }

    @Test
    public void testServiceBusSendAndReceiveWebSocketMessage() throws InterruptedException {
        assertThat(isPortAvailable(5671)).isTrue();
    }

    private static void bindPort(String host, int port) throws Exception {
        Socket s = new Socket();
        s.bind(new InetSocketAddress(host, port));
        s.close();
    }

    public static boolean isPortAvailable(int port) {
        Socket s = new Socket();
        try {
            bindPort("0.0.0.0", port);
            bindPort(InetAddress.getLocalHost().getHostAddress(), port);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public static void main(String[] args) {
        System.out.println(isPortAvailable(5671));
    }
}
