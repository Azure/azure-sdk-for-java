// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.test.eventhubs.stream.binder.health;

import com.azure.spring.test.AppRunner;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.web.client.RestTemplate;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class EventHubBinderHealthIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubBinderHealthIT.class);

    private RestTemplate restTemplate = new RestTemplate();

    @Test
    public void testSpringBootActuatorHealth() {
        LOGGER.info("testSpringBootActuatorHealth begin.");
        try (AppRunner app = new AppRunner(Application.class)) {

            app.property("spring.cloud.azure.eventhub.checkpoint-container", "container-name");
            app.property("spring.cloud.stream.bindings.consume-in-0.destination", "event-hub-name");
            app.property("spring.cloud.stream.bindings.supply-out-0.destination", "event-hub-name");

            app.property("management.endpoint.health.show-details", "always");
            app.property("management.endpoints.web.exposure.include", "*");

            app.start();

            final String response = restTemplate.getForObject(
                "http://localhost:" + app.port() + "/actuator/health/binders", String.class);
            assertEquals("{\"status\":\"UP\",\"components\":{\"eventhub\":{\"status\":\"UP\"}}}", response);

            LOGGER.info("response = {}", response);
        }
        LOGGER.info("testSpringBootActuatorHealth end.");
    }


    @SpringBootApplication
    public static class Application {

        private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

        @Bean
        public Consumer<Message<String>> consume() {
            return message -> {
                LOGGER.info("Message received: {}", message);
            };
        }

    }

}
