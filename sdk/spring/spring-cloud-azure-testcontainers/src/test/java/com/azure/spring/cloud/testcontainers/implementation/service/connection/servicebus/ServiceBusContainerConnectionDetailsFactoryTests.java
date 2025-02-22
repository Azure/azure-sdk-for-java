// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.testcontainers.implementation.service.connection.servicebus;

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
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;

@SpringJUnitConfig
@Testcontainers
//@EnabledOnOs(OS.LINUX)
class ServiceBusContainerConnectionDetailsFactoryTests {

    private static final Network network = Network.newNetwork();

    private static final int AZURE_SERVICEBUS_PORT = 5672;

    private static MSSQLServerContainer<?> sqlserver = new MSSQLServerContainer<>(
        "mcr.microsoft.com/mssql/server:2022-CU14-ubuntu-22.04")
        .acceptLicense()
        .withNetwork(network)
        .withNetworkAliases("sqlserver");

    @Container
    @ServiceConnection
    private static final GenericContainer<?> serviceBus = new GenericContainer<>(
        "mcr.microsoft.com/azure-messaging/servicebus-emulator:latest")
        .withCopyFileToContainer(MountableFile.forClasspathResource("servicebus/Config.json"),
            "/ServiceBus_Emulator/ConfigFiles/Config.json")
        .withExposedPorts(AZURE_SERVICEBUS_PORT)
        .waitingFor(Wait.forLogMessage(".*Emulator Service is Successfully Up!.*", 1))
        .withNetwork(network)
        .withEnv("SQL_SERVER", "sqlserver")
        .withEnv("MSSQL_SA_PASSWORD", sqlserver.getPassword())
        .withEnv("ACCEPT_EULA", "Y")
        .dependsOn(sqlserver);

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
    @ImportAutoConfiguration(classes = {AzureGlobalPropertiesAutoConfiguration.class, AzureServiceBusAutoConfiguration.class})
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
