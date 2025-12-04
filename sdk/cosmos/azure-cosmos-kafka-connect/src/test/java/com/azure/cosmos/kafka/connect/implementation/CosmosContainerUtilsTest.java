// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.kafka.connect.KafkaCosmosTestConfigurations;
import com.azure.cosmos.kafka.connect.KafkaCosmosTestSuiteBase;
import org.apache.kafka.connect.errors.ConnectException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CosmosContainerUtilsTest extends KafkaCosmosTestSuiteBase {
    private CosmosAsyncClient client;

    @BeforeMethod(groups = { "kafka-emulator" })
    public void beforeMethod() {
        this.client = new CosmosClientBuilder()
            .endpoint(KafkaCosmosTestConfigurations.HOST)
            .key(KafkaCosmosTestConfigurations.MASTER_KEY)
            .buildAsyncClient();
    }

    @AfterMethod(groups = { "kafka-emulator" }, alwaysRun = true)
    public void afterMethod() {
        // Clean up test containers
        if (this.client != null) {
            this.client.close();
        }
    }

    @Test(groups = { "kafka-emulator" })
    public void validateDatabaseAndContainers_WithNullContainerList_ReturnsAllContainers() {
        // Act
        List<String> result = CosmosContainerUtils.validateDatabaseAndContainers(
            null,
            client,
            databaseName);

        // Assert
        assertThat(result.contains(singlePartitionContainerName)).isTrue();
        assertThat(result.contains(multiPartitionContainerName)).isTrue();
        assertThat(result.contains(multiPartitionContainerWithIdAsPartitionKeyName)).isTrue();
    }

    @Test(groups = { "kafka-emulator" })
    public void validateDatabaseAndContainers_WithEmptyContainerList_ReturnsAllContainers() {
        // Act
        List<String> result = CosmosContainerUtils.validateDatabaseAndContainers(
            Collections.emptyList(),
            client,
            databaseName);

        // Assert
        assertThat(result.contains(singlePartitionContainerName)).isTrue();
        assertThat(result.contains(multiPartitionContainerName)).isTrue();
        assertThat(result.contains(multiPartitionContainerWithIdAsPartitionKeyName)).isTrue();
    }

    @Test(groups = { "kafka-emulator" })
    public void validateDatabaseAndContainers_WithSpecificContainers_ValidatesAndReturnsAllContainers() {
        // Act
        List<String> result = CosmosContainerUtils.validateDatabaseAndContainers(
            Arrays.asList(singlePartitionContainerName),
            client,
            databaseName);

        // Assert - should return all containers even when specific ones are requested
        assertThat(result).contains(singlePartitionContainerName);
    }

    @Test(groups = { "kafka-emulator" })
    public void validateDatabaseAndContainers_WithNonExistentContainers_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> CosmosContainerUtils.validateDatabaseAndContainers(
            Arrays.asList("nonexistent-container"),
            client,
            databaseName))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Containers specified in the config do not exist in the CosmosDB account.");
    }

    @Test(groups = { "kafka-emulator" })
    public void validateDatabaseAndContainers_WithWrongDatabase_ThrowsException() {
        // Act & Assert
        String wrongDatabaseName = "wrong-database-" + UUID.randomUUID();
        assertThatThrownBy(() -> CosmosContainerUtils.validateDatabaseAndContainers(
            Arrays.asList(singlePartitionContainerName),
            client,
            wrongDatabaseName))
            .isInstanceOf(ConnectException.class)
            .hasMessageContaining("Database specified in the config does not exist in the CosmosDB account");
    }
}