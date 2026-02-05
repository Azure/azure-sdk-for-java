// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.docker.compose.implementation.service.connection.bus;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.AzureServiceBusAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.AzureServiceBusMessagingAutoConfiguration;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
import com.azure.spring.messaging.servicebus.core.ServiceBusTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;

@SpringBootTest(properties = {
    "spring.docker.compose.skip.in-tests=false",
    "spring.docker.compose.file=classpath:com/azure/spring/cloud/docker/compose/implementation/service/connection/servicebus/servicebus-compose.yaml",
    "spring.docker.compose.stop.command=down",
    "spring.cloud.azure.servicebus.namespace=sbemulatorns",
    "spring.cloud.azure.servicebus.entity-name=queue.1",
    "spring.cloud.azure.servicebus.entity-type=queue",
    "spring.cloud.azure.servicebus.producer.entity-name=queue.1",
    "spring.cloud.azure.servicebus.producer.entity-type=queue",
    "spring.cloud.azure.servicebus.processor.entity-name=queue.1",
    "spring.cloud.azure.servicebus.processor.entity-type=queue"
})
@EnabledOnOs(OS.LINUX)
class ServiceBusDockerComposeConnectionDetailsFactoryTests {

    @Autowired
    private ServiceBusSenderClient senderClient;

    @Autowired
    private ServiceBusTemplate serviceBusTemplate;

    @Test
    void senderClientCanSendMessage() {
        // Wait for Service Bus emulator to be fully ready and queue entity to be available
        // The emulator depends on SQL Edge and needs time to initialize the messaging entities
        waitAtMost(Duration.ofSeconds(120)).pollInterval(Duration.ofSeconds(2)).untilAsserted(() -> {
            this.senderClient.sendMessage(new ServiceBusMessage("Hello World!"));
        });

        waitAtMost(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(Config.MESSAGES).contains("Hello World!");
        });
    }

    @Test
    void serviceBusTemplateCanSendMessage() {
        // Wait for Service Bus emulator to be fully ready and queue entity to be available
        // The emulator depends on SQL Edge and needs time to initialize the messaging entities
        waitAtMost(Duration.ofSeconds(120)).pollInterval(Duration.ofSeconds(2)).untilAsserted(() -> {
            this.serviceBusTemplate.sendAsync("queue.1", MessageBuilder.withPayload("Hello from ServiceBusTemplate!").build()).block();
        });

        waitAtMost(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(Config.MESSAGES).contains("Hello from ServiceBusTemplate!");
        });
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration(classes = {
        AzureGlobalPropertiesAutoConfiguration.class,
        AzureServiceBusAutoConfiguration.class,
        AzureServiceBusMessagingAutoConfiguration.class})
    static class Config {

        private static final Set<String> MESSAGES = ConcurrentHashMap.newKeySet();

        @Bean
        ServiceBusRecordMessageListener processMessage() {
            return context -> {
                MESSAGES.add(context.getMessage().getBody().toString());
            };
        }

        @Bean
        ServiceBusErrorHandler errorHandler() {
            // No-op error handler for tests: acknowledge errors without affecting test execution.
            return (context) -> {
            };
        }
    }
}
