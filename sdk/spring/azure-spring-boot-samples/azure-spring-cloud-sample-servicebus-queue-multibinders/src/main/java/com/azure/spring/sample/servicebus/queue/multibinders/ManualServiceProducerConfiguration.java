// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.servicebus.queue.multibinders;

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
public class ManualServiceProducerConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusQueueMultiBindersApplication.class);

    @Bean
    public EmitterProcessor<Message<String>> emitterProcessor1() {
        return EmitterProcessor.create();
    }

    @Bean
    public EmitterProcessor<Message<String>> emitterProcessor2() {
        return EmitterProcessor.create();
    }

    @Bean
    public Supplier<Flux<Message<String>>> supply1(EmitterProcessor<Message<String>> emitterProcessor1) {
        return () -> Flux.from(emitterProcessor1)
                         .doOnNext(m -> LOGGER.info("Manually sending message1 {}", m))
                         .doOnError(t -> LOGGER.error("Error encountered", t));
    }

    @Bean
    public Supplier<Flux<Message<String>>> supply2(EmitterProcessor<Message<String>> emitterProcessor2) {
        return () -> Flux.from(emitterProcessor2)
                         .doOnNext(m -> LOGGER.info("Manually sending message2 {}", m))
                         .doOnError(t -> LOGGER.error("Error encountered", t));
    }
}
