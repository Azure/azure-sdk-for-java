// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.docker.compose.implementation.service.connection.eventhubs;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.AzureEventHubsAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Collections;

import static org.awaitility.Awaitility.waitAtMost;

@SpringBootTest(properties = {
    "spring.docker.compose.skip.in-tests=false",
    "spring.docker.compose.file=classpath:com/azure/spring/cloud/docker/compose/implementation/service/connection/eventhubs/eventhubs-compose.yaml",
    "spring.docker.compose.stop.command=down",
    "spring.docker.compose.readiness.timeout=PT5M",
    "spring.cloud.azure.eventhubs.namespace=emulatorns1",
    "spring.cloud.azure.eventhubs.event-hub-name=eh1",
    "spring.cloud.azure.eventhubs.producer.event-hub-name=eh1"
})
@EnabledOnOs(OS.LINUX)
class EventHubsDockerComposeConnectionDetailsFactoryTests {

    @Autowired
    private EventHubProducerClient producerClient;

    @Test
    void producerClientCanSendMessage() {
        // Wait for Event Hubs emulator to be fully ready and event hub entity to be available
        waitAtMost(Duration.ofSeconds(240)).pollDelay(Duration.ofSeconds(5)).pollInterval(Duration.ofSeconds(2)).untilAsserted(() -> {
            EventData event = new EventData("Hello World!");
            this.producerClient.send(Collections.singletonList(event));
        });
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration(classes = {
        AzureGlobalPropertiesAutoConfiguration.class,
        AzureEventHubsAutoConfiguration.class})
    static class Config {
    }
}
