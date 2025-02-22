// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.docker.compose.implementation.service.connection.servicebus;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.AzureServiceBusAutoConfiguration;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;

@SpringBootTest(properties = {
    "spring.docker.compose.skip.in-tests=false",
    "spring.docker.compose.file=classpath:com/azure/spring/cloud/docker/compose/implementation/service/connection/servicebus/servicebus-compose.yaml",
    "spring.docker.compose.stop.command=down"
})
@EnabledOnOs(OS.LINUX)
class ServiceBusDockerComposeConnectionDetailsFactoryTests {

    @Autowired
    private ServiceBusSenderClient senderClient;

    @Test
    void contextLoads() {
        this.senderClient.sendMessage(new ServiceBusMessage("Hello World!"));

        waitAtMost(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(Config.messages).hasSize(1);
            assertThat(Config.messages.get(0).getBody().toString()).isEqualTo("Hello World!");
        });
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration(classes = {
        AzureGlobalPropertiesAutoConfiguration.class,
        AzureServiceBusAutoConfiguration.class})
    static class Config {

        static final List<ServiceBusReceivedMessage> messages = new ArrayList<>();

        @Bean
        ServiceBusRecordMessageListener processMessage() {
            return context -> {
                messages.add(context.getMessage());
            };
        }

        @Bean
        ServiceBusErrorHandler errorHandler() {
            return (context) -> {
                throw new RuntimeException("Error processing message: " + context.getException().getMessage());
            };
        }
    }
}
