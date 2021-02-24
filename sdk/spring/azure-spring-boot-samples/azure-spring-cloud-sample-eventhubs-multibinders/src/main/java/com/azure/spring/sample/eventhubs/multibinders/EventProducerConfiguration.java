// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.eventhubs.multibinders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Supplier;

@Profile("!manual")
@Configuration
public class EventProducerConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubMultiBindersApplication.class);

    private int i = 0;
    private int j = 0;

    @Bean
    public Supplier<Message<String>> supply1() {
        return () -> {
            LOGGER.info("Sending message1, sequence1 " + i);
            return MessageBuilder.withPayload("Hello world1, " + i++).build();
        };
    }

    @Bean
    public Supplier<Message<String>> supply2() {
        return () -> {
            LOGGER.info("Sending message2, sequence2 " + j);
            return MessageBuilder.withPayload("Hello world2, " + j++).build();
        };
    }
}

