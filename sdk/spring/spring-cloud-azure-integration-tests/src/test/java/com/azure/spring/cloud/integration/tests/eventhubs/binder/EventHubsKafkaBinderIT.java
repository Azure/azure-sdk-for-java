// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.integration.tests.eventhubs.binder;

import com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureEventHubsKafkaBinderOAuth2AutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureEventHubsKafkaOAuth2AutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaSpringCloudStreamConfiguration;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
                classes = {AzureEventHubsKafkaBinderOAuth2AutoConfiguration.class,
                           AzureEventHubsKafkaOAuth2AutoConfiguration.class,
                           AzureKafkaSpringCloudStreamConfiguration.class,
                           EventHubsKafkaBinderIT.TestConfig.class,})
@ActiveProfiles("eventhubs-kafka-binder")
class EventHubsKafkaBinderIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubsKafkaBinderIT.class);
    private static final String MESSAGE = UUID.randomUUID().toString();
    private static final CountDownLatch LATCH = new CountDownLatch(1);

    @Autowired
    private Sinks.Many<Message<String>> many;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }

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
                LATCH.countDown();
            };
        }
    }

    @Test
    void testSendAndReceiveMessage() throws InterruptedException {
        LOGGER.info("EventHubsKafkaBinderIT begin.");
        // Wait for Kafka Binder initialization to complete
        Thread.sleep(20000);
        LOGGER.info("Send a message:" + MESSAGE + ".");
        many.emitNext(new GenericMessage<>(MESSAGE), Sinks.EmitFailureHandler.FAIL_FAST);
        assertThat(EventHubsKafkaBinderIT.LATCH.await(40, TimeUnit.SECONDS)).isTrue();
        LOGGER.info("EventHubsKafkaBinderIT end.");
    }
}
