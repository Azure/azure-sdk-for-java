// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.testcontainers.implementation.service.connection.bus;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.AzureServiceBusAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.AzureServiceBusMessagingAutoConfiguration;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
import com.azure.spring.messaging.servicebus.core.ServiceBusTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.azure.ServiceBusEmulatorContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;

@SpringJUnitConfig
@TestPropertySource(properties = { "spring.cloud.azure.servicebus.entity-name=queue.1",
    "spring.cloud.azure.servicebus.entity-type=queue" })
@Testcontainers
@EnabledOnOs(OS.LINUX)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServiceBusContainerConnectionDetailsFactoryTests {

    private static final Network NETWORK = Network.newNetwork();

    private static final MSSQLServerContainer<?> SQLSERVER = new MSSQLServerContainer<>(
        "mcr.microsoft.com/mssql/server:2022-CU14-ubuntu-22.04")
        .acceptLicense()
        .withNetwork(NETWORK)
        .withNetworkAliases("sqlserver");

    @Container
    @ServiceConnection
    private static final ServiceBusEmulatorContainer SERVICE_BUS = new ServiceBusEmulatorContainer(
        "mcr.microsoft.com/azure-messaging/servicebus-emulator:latest")
        .acceptLicense()
        .withCopyFileToContainer(MountableFile.forClasspathResource("servicebus/Config.json"),
            "/ServiceBus_Emulator/ConfigFiles/Config.json")
        .withNetwork(NETWORK)
        .withMsSqlServerContainer(SQLSERVER);

    @Autowired
    private ServiceBusSenderClient senderClient;

    @Autowired
    private ServiceBusTemplate serviceBusTemplate;

    @Autowired
    private ServiceBusProcessorClient processorClient;

    @BeforeEach
    void setUp() throws InterruptedException {
        // Wait for any in-flight messages to be processed
        int previousSize;
        do {
            previousSize = Config.MESSAGES.size();
            Thread.sleep(2000);
        } while (Config.MESSAGES.size() != previousSize);

        // Clear messages for the next test
        Config.MESSAGES.clear();
    }

    @Test
    @Order(1)
    void senderClientCanSendMessage() {
        this.senderClient.sendMessage(new ServiceBusMessage("Hello World!"));

        waitAtMost(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(Config.MESSAGES).hasSize(1);
            assertThat(Config.MESSAGES.get(0).getBody().toString()).isEqualTo("Hello World!");
        });
    }

    @Test
    @Order(2)
    void serviceBusTemplateCanSendMessage() {
        this.serviceBusTemplate.sendAsync("queue.1", MessageBuilder.withPayload("Hello from ServiceBusTemplate!").build()).block();

        waitAtMost(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(Config.MESSAGES).hasSize(1);
            assertThat(Config.MESSAGES.get(0).getBody().toString()).isEqualTo("Hello from ServiceBusTemplate!");
        });
    }

    @Test
    @Order(3)
    void processorClientCanProcessMessages() {
        // The processor client is already running and listening to messages
        // (auto-started by ServiceBusProcessorClientLifecycleManager)
        // Send a message using the sender
        this.senderClient.sendMessage(new ServiceBusMessage("Hello from Processor test!"));

        // The processor will handle it through the registered listener
        waitAtMost(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(Config.MESSAGES).anySatisfy(msg ->
                assertThat(msg.getBody().toString()).isEqualTo("Hello from Processor test!")
            );
        });
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration(classes = {AzureGlobalPropertiesAutoConfiguration.class,
        AzureServiceBusAutoConfiguration.class,
        AzureServiceBusMessagingAutoConfiguration.class})
    static class Config {

        private static final List<ServiceBusReceivedMessage> MESSAGES = new ArrayList<>();

        @Bean
        ServiceBusRecordMessageListener processMessage() {
            return context -> {
                MESSAGES.add(context.getMessage());
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
