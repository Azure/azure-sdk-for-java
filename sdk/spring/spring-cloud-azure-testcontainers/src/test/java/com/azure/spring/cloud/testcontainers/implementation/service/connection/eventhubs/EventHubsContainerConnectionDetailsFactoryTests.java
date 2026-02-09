// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.testcontainers.implementation.service.connection.eventhubs;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.AzureEventHubsAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.azure.EventHubsEmulatorContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.util.Collections;

import static org.awaitility.Awaitility.waitAtMost;

@SpringJUnitConfig
@TestPropertySource(properties = { "spring.cloud.azure.eventhubs.event-hub-name=eh1" })
@Testcontainers
@EnabledOnOs(OS.LINUX)
class EventHubsContainerConnectionDetailsFactoryTests {

    private static final Network NETWORK = Network.newNetwork();

    @Container
    private static final GenericContainer<?> AZURITE = new GenericContainer<>("mcr.microsoft.com/azure-storage/azurite:latest")
        .withExposedPorts(10000, 10001, 10002)
        .withNetwork(NETWORK)
        .withNetworkAliases("azurite");

    @Container
    @ServiceConnection
    private static final EventHubsEmulatorContainer EVENT_HUBS = new EventHubsEmulatorContainer(
        "mcr.microsoft.com/azure-messaging/eventhubs-emulator:latest")
        .acceptLicense()
        .withCopyFileToContainer(MountableFile.forClasspathResource("eventhubs/Config.json"),
            "/Eventhubs_Emulator/ConfigFiles/Config.json")
        .withNetwork(NETWORK)
        .withEnv("BLOB_SERVER", "azurite")
        .withEnv("METADATA_SERVER", "azurite");

    @Autowired
    private EventHubProducerClient producerClient;

    @Test
    void producerClientCanSendMessage() {
        // Wait for Event Hubs emulator to be fully ready and event hub entity to be available
        waitAtMost(Duration.ofSeconds(120)).pollInterval(Duration.ofSeconds(2)).untilAsserted(() -> {
            EventData event = new EventData("Hello World!");
            this.producerClient.send(Collections.singletonList(event));
        });
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration(classes = {AzureGlobalPropertiesAutoConfiguration.class,
        AzureEventHubsAutoConfiguration.class})
    static class Config {
    }
}
