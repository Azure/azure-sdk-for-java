// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.eventhubs.binder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.util.function.Supplier;

@Configuration
@Profile("manual")
public class ManualEventProducerConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubBinderApplication.class);

    @Bean
    public EmitterProcessor<Message<String>> emitter() {
        return EmitterProcessor.create();
    }

    @Bean
    public Supplier<Flux<Message<String>>> supply(EmitterProcessor<Message<String>> emitter) {
        return () -> Flux.from(emitter)
                         .doOnNext(m -> LOGGER.info("Manually sending message {}", m))
                         .doOnError(t -> LOGGER.error("Error encountered", t));
    }
}
