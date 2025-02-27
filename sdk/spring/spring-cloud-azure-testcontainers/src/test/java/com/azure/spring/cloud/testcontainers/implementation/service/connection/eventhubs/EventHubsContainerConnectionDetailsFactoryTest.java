// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.testcontainers.implementation.service.connection.eventhubs;

import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.AzureEventHubsAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.azure.AzuriteContainer;
import org.testcontainers.azure.EventHubsEmulatorContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.awaitility.Awaitility;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@TestPropertySource(properties = {"spring.cloud.azure.eventhubs.event-hub-name=eh1", "spring.cloud.azure.eventhubs.consumer.consumer-group=$default"})
@Testcontainers
class EventHubsContainerConnectionDetailsFactoryTest {

    private static final Network network = Network.newNetwork();

    @Container
    private static final AzuriteContainer azurite = new AzuriteContainer(
            "mcr.microsoft.com/azure-storage/azurite:latest")
            .withNetwork(network);

    @Container
    @ServiceConnection
    private static final EventHubsEmulatorContainer eventHubs = new EventHubsEmulatorContainer(
            "mcr.microsoft.com/azure-messaging/eventhubs-emulator:latest")
            .acceptLicense()
            .withCopyFileToContainer(MountableFile.forClasspathResource("Config.json"),
                    "/Eventhubs_Emulator/ConfigFiles/Config.json")
            .withNetwork(network)
            .withAzuriteContainer(azurite);

    @Autowired
    private EventHubProducerClient producerClient;

    @Autowired
    private EventHubConsumerClient consumerClient;

    @Test
    void test() {
        this.producerClient.send(List.of(new EventData("test message")));

        Awaitility.waitAtMost(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(5)).untilAsserted(() -> {
            IterableStream<PartitionEvent> events = this.consumerClient.receiveFromPartition("0", 1,
                    EventPosition.earliest(), Duration.ofSeconds(2));
            Iterator<PartitionEvent> iterator = events.stream().iterator();
            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next().getData().getBodyAsString()).isEqualTo("test message");
        });
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration(classes = {AzureGlobalPropertiesAutoConfiguration.class, AzureEventHubsAutoConfiguration.class})
    static class Config {

    }

}
