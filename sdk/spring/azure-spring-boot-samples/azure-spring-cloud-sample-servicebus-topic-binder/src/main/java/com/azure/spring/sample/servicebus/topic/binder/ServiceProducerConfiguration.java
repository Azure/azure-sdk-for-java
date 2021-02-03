// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.servicebus.topic.binder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Supplier;

@Configuration
@Profile("!manual")
public class ServiceProducerConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusTopicBinderApplication.class);

    private int i = 0;

    @Bean
    public Supplier<Message<String>> supply() {
        return () -> {
            LOGGER.info("Sending message, sequence " + i);
            return MessageBuilder.withPayload("Hello world, " + i++).build();
        };
    }
}
