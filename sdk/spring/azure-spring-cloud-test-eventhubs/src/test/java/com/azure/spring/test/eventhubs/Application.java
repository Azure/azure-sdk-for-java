// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.test.eventhubs;

import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.api.reactor.Checkpointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.concurrent.Exchanger;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SpringBootApplication
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static final Exchanger<String> EXCHANGER = new Exchanger<>();

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

    @ServiceActivator(inputChannel = "test-eventhub.$Default.errors")
    public void consumeError(Message<?> message) throws InterruptedException {
        EXCHANGER.exchange("ERROR!");
    }
}
