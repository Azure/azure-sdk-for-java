// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.eventhubs.multibinders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

@Configuration
@Profile("manual")
public class ManualEventProducerConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubMultiBindersApplication.class);

    @Bean
    public Sinks.Many<Message<String>> many1() {
        return Sinks.many().unicast().onBackpressureBuffer();
    }

    @Bean
    public Sinks.Many<Message<String>> many2() {
        return Sinks.many().unicast().onBackpressureBuffer();
    }

    @Bean
    public Supplier<Flux<Message<String>>> supply1(Sinks.Many<Message<String>> many1) {
        return () -> many1.asFlux()
                          .doOnNext(m -> LOGGER.info("Manually sending message1 {}", m))
                          .doOnError(t -> LOGGER.error("Error encountered", t));
    }

    @Bean
    public Supplier<Flux<Message<String>>> supply2(Sinks.Many<Message<String>> many2) {
        return () -> many2.asFlux()
                          .doOnNext(m -> LOGGER.info("Manually sending message2 {}", m))
                          .doOnError(t -> LOGGER.error("Error encountered", t));
    }

}
