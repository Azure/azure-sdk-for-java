// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.testcontainers.implementation.service.connection.bus;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.AzureServiceBusAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.AzureServiceBusMessagingAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusConnectionDetails;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
import com.azure.spring.messaging.servicebus.core.ServiceBusTemplate;
import org.junit.jupiter.api.Test;
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;

@SpringJUnitConfig
@TestPropertySource(properties = { "spring.cloud.azure.servicebus.entity-name=queue.1",
    "spring.cloud.azure.servicebus.entity-type=queue" })
@Testcontainers
@EnabledOnOs(OS.LINUX)
@SuppressWarnings("deprecation") // Link to related issue: https://github.com/testcontainers/testcontainers-java/issues/11554
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
    private AzureServiceBusConnectionDetails connectionDetails;

    @Autowired
    private ServiceBusSenderClient senderClient;

    @Autowired
    private ServiceBusTemplate serviceBusTemplate;

    @Test
    void connectionDetailsShouldBeProvidedByFactory() {
        assertThat(connectionDetails).isNotNull();
        assertThat(connectionDetails.getConnectionString())
            .isNotBlank()
            .startsWith("Endpoint=sb://");
    }

    @Test
    void senderClientCanSendMessage() {
        // Wait for Service Bus emulator to be fully ready and queue entity to be available
        waitAtMost(Duration.ofSeconds(120)).pollInterval(Duration.ofSeconds(2)).untilAsserted(() -> {
            this.senderClient.sendMessage(new ServiceBusMessage("Hello World!"));
        });

        waitAtMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(Config.MESSAGES).contains("Hello World!");
        });
    }

    @Test
    void serviceBusTemplateCanSendMessage() {
        // Wait for Service Bus emulator to be fully ready and queue entity to be available
        waitAtMost(Duration.ofSeconds(120)).pollInterval(Duration.ofSeconds(2)).untilAsserted(() -> {
            this.serviceBusTemplate.sendAsync("queue.1", MessageBuilder.withPayload("Hello from ServiceBusTemplate!").build()).block();
        });

        waitAtMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(Config.MESSAGES).contains("Hello from ServiceBusTemplate!");
        });
    }


    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration(classes = {AzureGlobalPropertiesAutoConfiguration.class,
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
