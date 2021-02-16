// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.eventhubs.multibinders;

import com.azure.spring.integration.core.api.reactor.Checkpointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

import static com.azure.spring.integration.core.AzureHeaders.CHECKPOINTER;

@SpringBootApplication
public class EventHubMultiBindersApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubMultiBindersApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EventHubMultiBindersApplication.class, args);
    }

    @Bean
    public Consumer<Message<String>> consume1() {
        return message -> {
            Checkpointer checkpointer = (Checkpointer) message.getHeaders().get(CHECKPOINTER);
            LOGGER.info("New message1 received: '{}'", message);
            checkpointer.success()
                        .doOnSuccess(success -> LOGGER.info("Message1 '{}' successfully checkpointed", message))
                        .doOnError(error -> LOGGER.error("Exception: {}", error.getMessage()))
                        .subscribe();
        };
    }

    @Bean
    public Consumer<Message<String>> consume2() {
        return message -> {
            Checkpointer checkpointer = (Checkpointer) message.getHeaders().get(CHECKPOINTER);
            LOGGER.info("New message2 received: '{}'", message);
            checkpointer.success()
                        .doOnSuccess(success -> LOGGER.info("Message2 '{}' successfully checkpointed", message))
                        .doOnError(error -> LOGGER.error("Exception: {}", error.getMessage()))
                        .subscribe();
        };
    }

}
