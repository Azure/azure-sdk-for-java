// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.testcontainers.service.connection.storage;

import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.AzureStorageQueueAutoConfiguration;
import com.azure.storage.queue.QueueClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@TestPropertySource(properties = "spring.cloud.azure.storage.queue.queue-name=devstoreaccount1/tc-queue")
@Testcontainers
class StorageQueueContainerConnectionDetailsFactoryTests {

    @Container
    @ServiceConnection
    private static final GenericContainer<?> azurite = new GenericContainer<>(
        "mcr.microsoft.com/azure-storage/azurite:latest")
        .withExposedPorts(10001);

    @Autowired
    private QueueClient queueClient;

    @Test
    void test() {
        this.queueClient.create();
        this.queueClient.sendMessage("Hello World!");

        var messageItem = this.queueClient.receiveMessage();
        assertThat(messageItem.getBody().toString()).isEqualTo("Hello World!");
    }

    @Configuration
    @ImportAutoConfiguration(classes = {AzureGlobalPropertiesAutoConfiguration.class, AzureStorageQueueAutoConfiguration.class})
    static class Config {

    }

}
